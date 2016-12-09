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

package com.griddynamics.jagger.coordinator.zookeeper;

import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * A simple API for interaction with zookeeper's znodes.
 */
public interface ZNode {

    void addNodeWatcher(Watcher watcher);

    void addChildrenWatcher(Watcher watcher);

    <T> T getObject(Class<T> clazz);

    String getString();

    void setString(String data);

    void setData(byte[] data);

    void setObject(Object object);

    boolean hasChild(String path, Watcher watcher);

    ZNode createChild(ZNodeParameters parameters);

    ZNode child(String path);

    void removeChild(String path);

    List<ZNode> children();

    List<ZNode> children(Watcher watcher);

    List<ZNode> firstLevelChildren();

    List<ZNode> firstLevelChildren(Watcher watcher);

    void remove();

    void removeWithChildren();

    boolean hasChild(String childPath);

    String getPath();

    String getShortPath();

    ZNodeLock obtainLock();

    boolean exists();
}