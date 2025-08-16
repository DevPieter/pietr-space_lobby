package nl.devpieter.models;

import java.util.UUID;

public class PlayerParkourStat {

    private final UUID playerUuid;
    private final String playerName;

    private final long startTime;
    private final long endTime;

    private final long finishTime;

    public PlayerParkourStat(UUID playerUuid, String playerName, long startTime, long endTime, long finishTime) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.finishTime = finishTime;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public String playerName() {
        return playerName;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    public long finishTime() {
        return finishTime;
    }
}
