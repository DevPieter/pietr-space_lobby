package nl.devpieter.models;

import net.minestom.server.coordinate.Pos;

import java.util.UUID;

public class PlayerParkourHolder {

    private final UUID parkourUuid;
    private final long startTime;

    private int checkpointIndex;

    private Pos entityPos;
    private int entityId;

    public PlayerParkourHolder(UUID parkourUuid, long startTime, int checkpointIndex, Pos entityPos, int entityId) {
        this.parkourUuid = parkourUuid;
        this.startTime = startTime;
        this.checkpointIndex = checkpointIndex;
        this.entityPos = entityPos;
        this.entityId = entityId;
    }

    public UUID parkourUuid() {
        return parkourUuid;
    }

    public long startTime() {
        return startTime;
    }

    public int checkpointIndex() {
        return checkpointIndex;
    }

    public void setCheckpointIndex(int checkpointIndex) {
        this.checkpointIndex = checkpointIndex;
    }

    public Pos entityPos() {
        return entityPos;
    }

    public void setEntityPos(Pos entityPos) {
        this.entityPos = entityPos;
    }

    public int entityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}