package nl.devpieter.features.mining.models;

import nl.devpieter.models.BlockHolder;

public class MiningHolder {

    private final BlockHolder holder;
    private final long startTime;
    private final int totalBreakTicks;

    private int previousStage = -1;

    public MiningHolder(BlockHolder holder, long startTime, int totalBreakTicks) {
        this.holder = holder;
        this.startTime = startTime;
        this.totalBreakTicks = totalBreakTicks;
    }

    public BlockHolder holder() {
        return holder;
    }

    public long startTime() {
        return startTime;
    }

    public int totalBreakTicks() {
        return totalBreakTicks;
    }

    public int previousStage() {
        return previousStage;
    }

    public void updatePreviousStage() {
        this.previousStage = stage();
    }

    public int stage() {
        return (int) ((elapsedTime() * 10) / totalBreakTime());
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public int totalBreakTime() {
        return totalBreakTicks * 50;
    }

    public boolean isComplete() {
        return elapsedTime() >= totalBreakTime();
    }
}
