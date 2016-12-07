/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.appenders.telemetry.appender;

public class FailedThresholdStat {
    private int failureCounter;
    private long timeInFailure;
    private long lastFailure;

    public void updateStats(long timestamp, boolean previousFailed) {
        if (previousFailed && lastFailure > 0 && timestamp > lastFailure) {
            timeInFailure += timestamp - lastFailure;
        }
        timeInFailure += (previousFailed && lastFailure > 0 && timestamp > lastFailure) ? timestamp - lastFailure : 1;
        failureCounter++;
        lastFailure = timestamp;
    }

    @Override
    public String toString() {
        return "FailedThresholdStat{" +
                "failureCounter=" + failureCounter +
                ", timeInFailure=" + timeInFailure +
                ", lastFailure=" + lastFailure +
                '}';
    }

    public int getFailureCounter() {
        return failureCounter;
    }

    public void setFailureCounter(int failureCounter) {
        this.failureCounter = failureCounter;
    }

    public long getTimeInFailure() {
        return timeInFailure;
    }

    public void setTimeInFailure(long timeInFailure) {
        this.timeInFailure = timeInFailure;
    }

    public long getLastFailure() {
        return lastFailure;
    }

    public void setLastFailure(long lastFailure) {
        this.lastFailure = lastFailure;
    }
}
