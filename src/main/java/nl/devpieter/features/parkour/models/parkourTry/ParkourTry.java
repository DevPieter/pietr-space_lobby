package nl.devpieter.features.parkour.models.parkourTry;

import nl.devpieter.features.parkour.enums.ParkourCancelReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ParkourTry {

    private final String parkourId;
    private final UUID playerUuid;

    private final long startTime;
    private long endTime;

    private boolean isCancelled;
    private ParkourCancelReason cancelReason;

    private final HashMap<Integer, ReachedNode> reachedNodesMap;
    private final HashMap<Integer, SkippedNode> skippedNodesMap;
    private final List<FailedNode> failedNodes;

    private int lastReachedNode;
    private int lastCheckpoint;

    public ParkourTry(String parkourId, UUID playerUuid) {
        this.parkourId = parkourId;
        this.playerUuid = playerUuid;

        startTime = System.currentTimeMillis();
        endTime = 0;

        isCancelled = false;
        cancelReason = ParkourCancelReason.None;

        reachedNodesMap = new HashMap<>();
        skippedNodesMap = new HashMap<>();
        failedNodes = new ArrayList<>();

        lastReachedNode = -1;
        lastCheckpoint = -1;
    }

    public void rollbackToLastCheckpoint() {
        if (lastCheckpoint < 0) {
            lastReachedNode = -1;
            lastCheckpoint = -1;

            reachedNodesMap.clear();
            skippedNodesMap.clear();
            return;
        }

        reachedNodesMap.entrySet().removeIf(entry -> entry.getKey() > lastCheckpoint);
        skippedNodesMap.entrySet().removeIf(entry -> entry.getKey() > lastCheckpoint);

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

    public void addReachedNode(ReachedNode node) {
        reachedNodesMap.put(node.toNodeIndex(), node);
    }

    public void addSkippedNode(SkippedNode node) {
        skippedNodesMap.put(node.nodeIndex(), node);
    }

    public void addFailedNode(FailedNode node) {
        failedNodes.add(node);
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
        return new ArrayList<>(reachedNodesMap.values());
    }

    public List<SkippedNode> skippedNodes() {
        return new ArrayList<>(skippedNodesMap.values());
    }

    public List<FailedNode> failedNodes() {
        return new ArrayList<>(failedNodes);
    }

    public boolean isReached(int nodeIndex) {
        return reachedNodesMap.containsKey(nodeIndex);
    }

    public void setReached(int nodeIndex, boolean isCheckpoint) {
        lastReachedNode = nodeIndex;
        if (isCheckpoint) lastCheckpoint = nodeIndex;
    }

    public long completionTime() {
        if (isCancelled) return -1;
        return endTime - startTime;
    }

    public boolean hasFinished() {
        return !isCancelled && endTime > 0;
    }
}
