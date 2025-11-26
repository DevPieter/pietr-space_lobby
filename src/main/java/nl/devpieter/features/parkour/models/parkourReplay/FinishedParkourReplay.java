package nl.devpieter.features.parkour.models.parkourReplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientInputPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.Mannequin;
import nl.devpieter.utils.minestom.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FinishedParkourReplay {

    private final List<TrackedPacket> trackedPackets = new ArrayList<>();

    private final Pos startPosition;
    private final Pos respawnPosition;

    private Task replayTask = null;

    public FinishedParkourReplay(ParkourReplay replay) {
        this.trackedPackets.addAll(replay.trackedPackets());
        this.startPosition = replay.startPosition();
        this.respawnPosition = replay.respawnPosition();
    }

    public boolean startReplay(Player player) {
        if (replayTask != null) return false;

        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        AtomicReference<Integer> index = new AtomicReference<>(0);

        Mannequin mannequin = new Mannequin(player);
        mannequin.setInstance(player.getInstance(), startPosition);

        player.teleport(mannequin.getPosition().add(0, 5, 0));
        player.setGameMode(GameMode.SPECTATOR);

        replayTask = scheduler.scheduleTask(() -> {
            int i = index.get();
            if (i >= trackedPackets.size()) {
                mannequin.remove();

                player.setGameMode(GameMode.SURVIVAL);
                PlayerUtils.teleport(player, respawnPosition);
                scheduler.buildTask(player::stopSpectating).schedule();

                replayTask.cancel();
                replayTask = null;
                return;
            }

            TrackedPacket trackedPacket = trackedPackets.get(i);
            ClientPacket packet = trackedPacket.packet();

            if (packet instanceof ClientPlayerPositionPacket positionPacket) {
                mannequin.refreshPosition(positionPacket.position().asPos().withYaw(mannequin.getPosition().yaw()).withPitch(mannequin.getPosition().pitch()), false);
            } else if (packet instanceof ClientPlayerRotationPacket rotationPacket) {
                mannequin.refreshPosition(mannequin.getPosition().withYaw(rotationPacket.yaw()).withPitch(rotationPacket.pitch()), false);
            } else if (packet instanceof ClientPlayerPositionAndRotationPacket positionAndRotationPacket) {
                mannequin.refreshPosition(positionAndRotationPacket.position(), false);
            } else if (packet instanceof ClientInputPacket inputPacket) {
                mannequin.setSneaking(inputPacket.shift());
                mannequin.setSprinting(inputPacket.sprint());
            }

            index.updateAndGet(v -> v + 1);
        }, TaskSchedule.immediate(), TaskSchedule.millis(1));

        return true;
    }
}
