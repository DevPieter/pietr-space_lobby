package nl.devpieter.features.parkour;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.timer.Task;
import nl.devpieter.features.parkour.models.parkour.Parkour;
import nl.devpieter.features.parkour.models.parkourReplay.FinishedParkourReplay;
import nl.devpieter.features.parkour.models.parkourReplay.ParkourReplay;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ParkourReplayManager {

    private static ParkourReplayManager INSTANCE;

    private final List<String> trackedPacketNames = List.of(
            "ClientInputPacket",
            "ClientPlayerPositionPacket",
            "ClientPlayerRotationPacket",
            "ClientPlayerPositionAndRotationPacket"
    );

    private final HashMap<UUID, ParkourReplay> parkourReplays = new HashMap<>();
    private final HashMap<UUID, FinishedParkourReplay> finishedReplays = new HashMap<>();

    private final HashMap<UUID, Task> activeReplays = new HashMap<>();

    private ParkourReplayManager() {
    }

    public static ParkourReplayManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourReplayManager();
        return INSTANCE;
    }

    public @Nullable ParkourReplay getParkourReplay(Player player) {
        return parkourReplays.get(player.getUuid());
    }

    public @Nullable FinishedParkourReplay getFinishedReplay(UUID replayId) {
        return finishedReplays.get(replayId);
    }

    public void startTracking(Player player, Parkour parkour) {
        ParkourReplay replay = new ParkourReplay(player.getPosition(), parkour);
        parkourReplays.put(player.getUuid(), replay);

        replay.startTracking();
    }

    public @Nullable UUID stopTracking(Player player) {
        ParkourReplay replay = getParkourReplay(player);
        if (replay == null) return null;

        replay.stopTracking();
        parkourReplays.remove(player.getUuid());

        FinishedParkourReplay finishedReplay = new FinishedParkourReplay(replay);

        UUID id = UUID.randomUUID();
        finishedReplays.put(id, finishedReplay);

        return id;
    }

    public void playReplay(Player player, FinishedParkourReplay replay) {

    }

    public void tryPushPacket(Player player, ClientPacket packet) {
        ParkourReplay replay = getParkourReplay(player);
        if (replay == null) return;

        String packetName = packet.getClass().getSimpleName();
        if (!trackedPacketNames.contains(packetName)) return;

        replay.pushPacket(packet);
    }
}
