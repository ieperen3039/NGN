package NG.Tools;

public interface TickTime {

    /**
     * @return the time at the call of updateLooptime
     */
    float getTime();

    /**
     * @return The number of milliseconds between the previous two ticks.
     */
    long getDeltaTimeMillis();

    /**
     * @return The elapsed time in seconds between the previous two ticks.
     */
    double getDeltaTimeSeconds();

    /**
     * @return time since the start of the current tick in milliseconds.
     */
    long getMillisSinceLastUpdate();

    /**
     * @return time since the start of the current tick in seconds
     */
    double getSecondsSinceLastUpdate();

}