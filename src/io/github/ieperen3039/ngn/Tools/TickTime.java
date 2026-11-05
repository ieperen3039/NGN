package io.github.ieperen3039.ngn.Tools;

public interface TickTime {

    /**
     * @return the engine time in seconds since an implementation-defined epoch.
     */
    double getTime();

    /**
     * @return The time elapsed in seconds between the previous two ticks.
     */
    double getDeltaTimeSeconds();

    /**
     * @return time since the start of the current tick in seconds
     */
    double getSecondsSinceLastUpdate();

}