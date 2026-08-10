package nl.devpieter.features.parkour.models.setting;

import nl.devpieter.models.SimplePoint;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class PlayerParkourSettings {

    private final String parkourId;
    private final UUID playerUuid;

    private boolean useCustomRespawn;
    private @Nullable SimplePoint customRespawnPoint;

    public PlayerParkourSettings(String parkourId, UUID playerUuid) {
        this.parkourId = parkourId;
        this.playerUuid = playerUuid;
    }

    public String parkourId() {
        return parkourId;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

}
