package lib;

public class LamportClock {
    private int clock = 0;

    public LamportClock(int clockValue) {
        this.clock = clockValue;
    }

    public synchronized int getClock() {
        return clock;
    }

    public synchronized void incrementClock() {
        clock++;
    }

    public synchronized void updateClock(int sentClock) {
        clock = Math.max(clock, sentClock) + 1;
    }
}
