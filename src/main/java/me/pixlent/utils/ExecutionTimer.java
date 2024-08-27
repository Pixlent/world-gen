package me.pixlent.utils;

/**
 * A utility class to time how long something takes to execute
 * Initialize the object to begin, and run {@link #finished()}
 */
public class ExecutionTimer {
    private final long startTime = System.currentTimeMillis();

    /**
     * Run method when your execution has finished
     *
     * @return The time used for execution in milliseconds
     */
    public long finished() {
        return System.currentTimeMillis() - startTime;
    }
}