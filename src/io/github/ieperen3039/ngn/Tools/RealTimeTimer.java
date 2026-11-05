package io.github.ieperen3039.ngn.Tools;

/**
 * @author Geert van Ieperen adapter design pattern
 */
public class RealTimeTimer implements TickTimeTimer {
    private long currentTime;
    private long previousTime;
    private final long startTime;

    public RealTimeTimer() {
        startTime = System.currentTimeMillis();
        currentTime = 0;
        previousTime = 0;
    }

    /**
     * starts a new tick at the given time in milliseconds.
     *
     * @param newElement the new current value
     */
    public void updateTimeMillis(long newElement) {
        previousTime = currentTime;
        currentTime = newElement;
    }

    @Override
    public void updateTime(double newTime) {
        updateTimeMillis((long) (newTime * 1000));
    }

    /**
     * @return the time at the call of updateLooptime
     */
    @Override
    public double getTime() {
        return currentTime / 1000.0;
    }

    /**
     * @return The number of milliseconds between the previous two ticks.
     */
    public long getDeltaTimeMillis() {
        return currentTime - previousTime;
    }

    /**
     * @return The elapsed time in seconds between the previous two ticks.
     */
    @Override
    public double getDeltaTimeSeconds() {
        return getDeltaTimeMillis() / 1000.0;
    }

    /**
     * @return time since the start of the current tick in milliseconds.
     */
    public long getMillisSinceLastUpdate() {
        return getSystemElapsedMillis() - currentTime;
    }

    /**
     * @return time since the start of the current tick in seconds
     */
    @Override
    public double getSecondsSinceLastUpdate() {
        return getMillisSinceLastUpdate() / 1000.0;
    }

    /**
     * Adds a new tick at the current system time
     */
    @Override
    public void updateLoopTime() {
        updateTimeMillis(getSystemElapsedMillis());
    }

    /**
     * @return The number of milliseconds since the start of the program.
     */
    private long getSystemElapsedMillis() {
        return (System.currentTimeMillis() - startTime);
    }

}
