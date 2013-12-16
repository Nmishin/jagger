/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
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
package com.griddynamics.jagger.util;

import java.io.Serializable;
import java.util.Date;

/** Container to store information about environment on particular node
 * @author Dmitry Latnikov
 * @n
 * @par Details:
 * @details
 */
public class GeneralNodeInfo implements Serializable{

    public static final String NODE_INFO_MARKER = "NODE_INFO";

    private String nodeId;
    private long systemTime = 0;
    private String osName = "";
    private String osVersion = "";
    private String javaVersion = "";
    private String cpuModel = "";
    private int cpuMHz = 0;
    private int cpuTotalCores = 0;
    private int cpuTotalSockets = 0;
    private long systemRAM = 0;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getSystemRAM() {
        return systemRAM;
    }

    public void setSystemRAM(long systemRAM) {
        this.systemRAM = systemRAM;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public int getCpuTotalSockets() {
        return cpuTotalSockets;
    }

    public void setCpuTotalSockets(int cpuTotalSockets) {
        this.cpuTotalSockets = cpuTotalSockets;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public int getCpuMHz() {
        return cpuMHz;
    }

    public void setCpuMHz(int cpuMHz) {
        this.cpuMHz = cpuMHz;
    }

    public int getCpuTotalCores() {
        return cpuTotalCores;
    }

    public void setCpuTotalCores(int cpuTotalCores) {
        this.cpuTotalCores = cpuTotalCores;
    }

    @Override
    public String toString() {
        return "GeneralNodeInfo{" +
                "systemTime=" + new Date(systemTime).toString() +
                ", nodeId=" + nodeId +
                ", osName=" + osName +
                ", osVersion=" + osVersion +
                ", javaVersion=" + javaVersion +
                ", systemRAM=" + systemRAM +
                ", cpuModel=" + cpuModel +
                ", cpuMHz=" + cpuMHz +
                ", cpuTotalCores=" + cpuTotalCores +
                ", cpuTotalSockets=" + cpuTotalSockets +
                '}';
    }
}
