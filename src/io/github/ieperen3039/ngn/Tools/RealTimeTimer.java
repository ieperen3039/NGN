package io.github.ieperen3039.ngn.Tools;

/**
 * @author Geert van Ieperen adapter design pattern
 */
public class RealTimeTimer implements TickTime {
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
     * @param newElement the new current value
     */
    public void updateTimeMillis(long newElement) {
        previousTime = currentTime;
        currentTime = newElement;
    }

    /**
     * starts a new tick at the given deltaTime since the last tick.
     */
    public void addDeltaTime(double deltaTime) {
        updateTimeMillis(currentTime + (long) (deltaTime * 1000));
    }

    /**
     * @return the time at the call of updateLooptime
     */
    @Override
    public float getTime() {
        return currentTime / 1000f;
    }

    /**
     * @return The number of milliseconds between the previous two ticks.
     */
    @Override
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
    @Override
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
