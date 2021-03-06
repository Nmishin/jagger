/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the Apache License; either
 * version 2.0 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.engine.e1.process;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.griddynamics.jagger.agent.model.GetGeneralNodeInfo;
import com.griddynamics.jagger.coordinator.CommandExecutor;
import com.griddynamics.jagger.coordinator.ConfigurableWorker;
import com.griddynamics.jagger.coordinator.NodeContext;
import com.griddynamics.jagger.coordinator.Qualifier;
import com.griddynamics.jagger.kernel.WorkloadWorkerCommandExecutor;
import com.griddynamics.jagger.storage.fs.logging.LogWriter;
import com.griddynamics.jagger.util.ExceptionLogger;
import com.griddynamics.jagger.util.GeneralInfoCollector;
import com.griddynamics.jagger.util.GeneralNodeInfo;
import com.griddynamics.jagger.util.TimeoutsConfiguration;
import com.griddynamics.jagger.util.UrlClassLoaderHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Adapts {@link PerThreadWorkloadProcess} and {@link PeriodWorkloadProcess} to coordination API.
 */
public class WorkloadWorker extends ConfigurableWorker {
    private static final Logger log = LoggerFactory.getLogger(WorkloadWorker.class);
    private static final int CORE_POOL_SIZE = 5;
    private TimeoutsConfiguration timeoutsConfiguration;

    private GeneralInfoCollector generalInfoCollector = new GeneralInfoCollector();

    private Map<String, WorkloadProcess> processes = Maps.newConcurrentMap();
    private Map<String, Integer> pools = Maps.newConcurrentMap();

    private LogWriter logWriter;
    
    private UrlClassLoaderHolder classLoaderHolder;

    @Override
    public Collection<CommandExecutor<?, ?>> getExecutors() {
        return super.getExecutors();
    }
    
    public void setClassLoaderHolder(UrlClassLoaderHolder classLoaderHolder) {
        this.classLoaderHolder = classLoaderHolder;
    }
    
    @Required
    public void setTimeoutsConfiguration(TimeoutsConfiguration timeoutsConfiguration) {
        this.timeoutsConfiguration = timeoutsConfiguration;
    }

    @Override
    public void configure() {
        onCommandReceived(StartWorkloadProcess.class).execute(
                new WorkloadWorkerCommandExecutor<StartWorkloadProcess, String>() {

            @Override
            public Qualifier<StartWorkloadProcess> getQualifier() {
                return Qualifier.of(StartWorkloadProcess.class);
            }

            @Override
            public String doExecute(StartWorkloadProcess command, NodeContext nodeContext) {
                log.debug("Processing command {}", command);
                int poolSize = command.getPoolSize();

                if (poolSize < command.getThreads()) {
                    throw new IllegalStateException("Error! Pool size is less then thread count");
                }

                WorkloadProcess process;

                if (command.getScenarioContext().getWorkloadConfiguration().getPeriod() > 0) {
                    log.info("start periodic load process");
                    // start periodic process
                    process = new PeriodWorkloadProcess(command.getSessionId(), command, nodeContext,
                            getFixedThreadPoolExecutor(poolSize), timeoutsConfiguration);
                } else {
                    log.info("start per thread load process");
                    process = new PerThreadWorkloadProcess(command.getSessionId(), command, nodeContext,
                            getFixedThreadPoolExecutor(poolSize), timeoutsConfiguration);
                }
                String processId = generateId();
                processes.put(processId, process);
                pools.put(processId, poolSize);
                process.start();
                return processId;
            }
        }
        );

        onCommandReceived(ChangeWorkloadConfiguration.class).execute(
                new WorkloadWorkerCommandExecutor<ChangeWorkloadConfiguration, Boolean>() {

            @Override
            public Qualifier<ChangeWorkloadConfiguration> getQualifier() {
                return Qualifier.of(ChangeWorkloadConfiguration.class);
            }

            @Override
            public Boolean doExecute(ChangeWorkloadConfiguration command, NodeContext nodeContext) {
                Preconditions.checkArgument(command.getProcessId() != null, "Process id cannot be null");

                Integer poolSize = pools.get(command.getProcessId());
                if (poolSize < command.getConfiguration().getThreads()) {
                    throw new IllegalStateException("Error! Pool size is less then thread count");
                }

                WorkloadProcess process = getProcess(command.getProcessId());

                process.changeConfiguration(command.getConfiguration());

                return true;
            }
        }
        );

        onCommandReceived(PollWorkloadProcessStatus.class).execute(
                new WorkloadWorkerCommandExecutor<PollWorkloadProcessStatus, WorkloadStatus>() {

            @Override
            public Qualifier getQualifier() {
                return Qualifier.of(PollWorkloadProcessStatus.class);
            }

            @Override
            public WorkloadStatus doExecute(PollWorkloadProcessStatus command, NodeContext nodeContext) {
                Preconditions.checkArgument(command.getProcessId() != null, "Process id cannot be null");

                WorkloadProcess process = getProcess(command.getProcessId());
                return process.getStatus();
            }
        }
        );

        onCommandReceived(StopWorkloadProcess.class).execute(
                new WorkloadWorkerCommandExecutor<StopWorkloadProcess, WorkloadStatus>() {

            @Override
            public Qualifier<StopWorkloadProcess> getQualifier() {
                return Qualifier.of(StopWorkloadProcess.class);
            }

            @Override
            public WorkloadStatus doExecute(StopWorkloadProcess command, NodeContext nodeContext) {
                log.debug("Going to stop process {} on kernel {}", command.getProcessId(), nodeContext.getId().getIdentifier());

                Preconditions.checkArgument(command.getProcessId() != null, "Process id cannot be null");

                WorkloadProcess process = getProcess(command.getProcessId());
                process.stop();
                processes.remove(command.getProcessId());
                logWriter.flush();
                return process.getStatus();
            }
        });

        onCommandReceived(GetGeneralNodeInfo.class).execute(
                new WorkloadWorkerCommandExecutor<GetGeneralNodeInfo, GeneralNodeInfo>() {

                    @Override
                    public Qualifier<GetGeneralNodeInfo> getQualifier() {
                        return Qualifier.of(GetGeneralNodeInfo.class);
                    }

                    @Override
                    public GeneralNodeInfo doExecute(GetGeneralNodeInfo command, NodeContext nodeContext) {
                        long startTime = System.currentTimeMillis();
                        log.debug("start GetGeneralNodeInfo on kernel {}", nodeContext.getId());
                        GeneralNodeInfo generalNodeInfo = generalInfoCollector.getGeneralNodeInfo();
                        log.debug("finish GetGeneralNodeInfo on kernel {} time {} ms", nodeContext.getId(), System.currentTimeMillis() - startTime);
                        return generalNodeInfo;
                    }
                });
        
        onCommandReceived(AddUrlClassLoader.class).execute(new WorkloadWorkerCommandExecutor<AddUrlClassLoader, Boolean>() {
    
            @Override
            public Qualifier<AddUrlClassLoader> getQualifier() {
                return Qualifier.of(AddUrlClassLoader.class);
            }
    
            @Override
            public Boolean doExecute(AddUrlClassLoader command, NodeContext nodeContext) {
                if (classLoaderHolder != null) {
                    return classLoaderHolder.createFor(command.getClassesUrl());
                }
                return null;
            }
        });
        
        onCommandReceived(RemoveUrlClassLoader.class).execute(new WorkloadWorkerCommandExecutor<RemoveUrlClassLoader, Boolean>() {
    
            @Override
            public Qualifier<RemoveUrlClassLoader> getQualifier() {
                return Qualifier.of(RemoveUrlClassLoader.class);
            }
    
            @Override
            public Boolean doExecute(RemoveUrlClassLoader command, NodeContext nodeContext) {
                if (classLoaderHolder != null) {
                    return classLoaderHolder.clear();
                }
                return null;
            }
        });
    }

    /**
     * @param poolSize size of fixed thread pool to create
     * @return {@link ThreadPoolExecutor} instance with given pool size
     */
    private ThreadPoolExecutor getFixedThreadPoolExecutor(int poolSize) {
        int corePoolSize = poolSize < CORE_POOL_SIZE ? poolSize : CORE_POOL_SIZE;
        return new ThreadPoolExecutor(corePoolSize, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("workload-thread %d")
                        .setUncaughtExceptionHandler(ExceptionLogger.INSTANCE)
                        .build());
    }

    private String generateId() {
        return "WorkloadProcess-" + UUID.randomUUID();
    }

    public void setLogWriter(LogWriter logWriter) {
        this.logWriter = logWriter;
    }

    public LogWriter getLogWriter() {
        return logWriter;
    }

    public WorkloadProcess getProcess(String processId) {
        WorkloadProcess result = processes.get(processId);
        for (int i = 0; (result == null) && (i < 1000); ++i) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = processes.get(processId);
        }
        if (result == null) {
            throw new RuntimeException("Problem with process registration");
        }
        return result;
    }
}
