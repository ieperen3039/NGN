package io.github.ieperen3039.ngn.Tools;

public interface TickTimeTimer extends TickTime {

    /**
     * Sets the engine time to the given time in seconds since an implementation-defined epoch, and start a new tick.
     * This epoch is the same epoch used to calculate {@link getTime}
     */
    void updateTime(double newTime);

    /**
     * Create a new engine tick at deltaTime seconds since the last tick.
     */
    default void addDeltaTime(double deltaTime) {
        updateTime(getTime() + deltaTime);
    }

    /**
     * update the internal time in an implementation-defined manner, and start a new tick
     */
    void updateLoopTime();
}
