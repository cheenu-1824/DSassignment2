package lib;

/**
 * The LamportClock class is used as a Lamport logical clock for synchronization for the weather service.
 * Works by using logical timestamps to order events in a distributed system.
 */
public class LamportClock {
    private int clock = 0;

    /**
     * Constructs a new LamportClock instance with an initial clock value.
     *
     * @param clockValue The initial value for the Lamport clock.
     */
    public LamportClock(int clockValue) {
        this.clock = clockValue;
    }

    /**
     * Gets the current value of the Lamport clock.
     *
     * @return The current value of the Lamport clock.
     */
    public synchronized int getClock() {
        return clock;
    }

    /**
     * Increments the Lamport clock by one.
     */
    public synchronized void incrementClock() {
        clock++;
    }

    /**
     * Updates the Lamport clock based on the received Lamport clock value.
     * The updated value is set to the maximum of the current clock value and the received clock value, plus one.
     *
     * @param sentClock The Lamport clock value received from another process.
     */
    public synchronized void updateClock(int sentClock) {
        clock = Math.max(clock, sentClock) + 1;
    }
}
