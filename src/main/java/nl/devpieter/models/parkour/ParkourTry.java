package nl.devpieter.models.parkour;

import nl.devpieter.enums.ParkourCancelReason;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public class ParkourTry {
    private final BitSet reached = new BitSet();

    private final UUID parkourId;
    private final UUID playerUuid;

    private final long startTime;
    private long endTime;

    private boolean isCancelled;
    private ParkourCancelReason cancelReason = ParkourCancelReason.Unknown;

    private final List<ReachedNode> reachedNodes;
    private final List<FailedNode> failedNodes;

    private int lastReachedNode;
    private int lastCheckpoint;

    public ParkourTry(UUID parkourId, UUID playerUuid) {
        this.parkourId = parkourId;
        this.playerUuid = playerUuid;

        this.startTime = System.currentTimeMillis();
        this.endTime = 0;

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

    public void rollbackToCheckpoint(int checkpointIndex) {
        if (checkpointIndex < 0 || checkpointIndex >= reachedNodes.size()) {
            throw new IndexOutOfBoundsException("Checkpoint index out of bounds: " + checkpointIndex);
        }

        for (int i = 0; i < reachedNodes.size(); i++) {
            if (i <= checkpointIndex) reached.set(i);
            else reached.clear(i);
        }

        lastReachedNode = checkpointIndex;
        lastCheckpoint = checkpointIndex;
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

    public UUID parkourId() {
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

//    public void setLastReachedNode(int lastReachedNode) {
//        this.lastReachedNode = lastReachedNode;
//    }

    public int lastCheckpoint() {
        return lastCheckpoint;
    }

//    public void setLastCheckpoint(int lastCheckpoint) {
//        this.lastCheckpoint = lastCheckpoint;
//    }

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
