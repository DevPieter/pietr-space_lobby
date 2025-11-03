package nl.devpieter.features.parkour.models.parkourTry;

import nl.devpieter.features.parkour.enums.ParkourCancelReason;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public class ParkourTry {

    private final BitSet reached = new BitSet();

    private final String parkourId;
    private final UUID playerUuid;

    private final long startTime;
    private long endTime;

    private boolean isCancelled;
    private ParkourCancelReason cancelReason;

    private final List<ReachedNode> reachedNodes;
    private final List<FailedNode> failedNodes;

    private int lastReachedNode;
    private int lastCheckpoint;

    public ParkourTry(String parkourId, UUID playerUuid) {
        this.parkourId = parkourId;
        this.playerUuid = playerUuid;

        this.startTime = System.currentTimeMillis();
        this.endTime = 0;

        this.isCancelled = false;
        this.cancelReason = ParkourCancelReason.None;

        this.reachedNodes = new ArrayList<>();
        this.failedNodes = new ArrayList<>();

        this.lastReachedNode = -1;
        this.lastCheckpoint = -1;
    }

    public void resetProgress() {
        lastReachedNode = -1;
        lastCheckpoint = -1;

        reachedNodes.clear();
        reached.clear();
    }

    public void rollbackToLastCheckpoint() {
        if (lastCheckpoint < 0) return;

        for (int i = 0; i < reachedNodes.size(); i++) {
            if (i <= lastCheckpoint) reached.set(i);
            else reached.clear(i);
        }

        lastReachedNode = lastCheckpoint;
    }

    public void finish() {
        if (isCancelled) return;

        this.endTime = System.currentTimeMillis();
        this.cancelReason = ParkourCancelReason.None;
    }

    public void cancel(ParkourCancelReason reason) {
        if (isCancelled) return;

        this.endTime = System.currentTimeMillis();
        this.isCancelled = true;
        this.cancelReason = reason;
    }

    public String parkourId() {
        return parkourId;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public ParkourCancelReason cancelReason() {
        return cancelReason;
    }

    public int lastReachedNode() {
        return lastReachedNode;
    }

    public int lastCheckpoint() {
        return lastCheckpoint;
    }

    public List<ReachedNode> reachedNodes() {
        return reachedNodes;
    }

    public List<FailedNode> failedNodes() {
        return failedNodes;
    }

    public boolean isReached(int nodeIndex) {
        return reached.get(nodeIndex);
    }

    public void setReached(int nodeIndex, boolean isCheckpoint) {
        reached.set(nodeIndex);

        lastReachedNode = nodeIndex;
        if (isCheckpoint) lastCheckpoint = nodeIndex;
    }
}
