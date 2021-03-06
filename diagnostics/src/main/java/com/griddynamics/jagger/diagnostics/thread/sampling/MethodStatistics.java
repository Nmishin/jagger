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

package com.griddynamics.jagger.diagnostics.thread.sampling;

import java.io.Serializable;

public class MethodStatistics implements Serializable {
    private Method method;
    private long observations;
    private long onTopObservations;

    public MethodStatistics(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public long getObservations() {
        return observations;
    }

    public void setObservations(long observations) {
        this.observations = observations;
    }

    public void registerObservation() {
        observations++;
    }

    public void registerOnTopObservation() {
        onTopObservations++;
    }

    public long getOnTopObservations() {
        return onTopObservations;
    }

    public void setOnTopObservations(long onTopObservations) {
        this.onTopObservations = onTopObservations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodStatistics that = (MethodStatistics) o;

        if (!method.equals(that.method)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

}
