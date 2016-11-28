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