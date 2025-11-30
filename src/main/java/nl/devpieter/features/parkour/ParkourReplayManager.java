package nl.devpieter.features.parkour;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.features.parkour.models.parkourReplay.FinishedPlayerReplay;
import nl.devpieter.features.parkour.models.parkourReplay.PlayerReplay;
import nl.devpieter.features.parkour.models.parkourReplay.PlayerReplayFrame;
import nl.devpieter.models.TaskRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParkourReplayManager {

    private static ParkourReplayManager INSTANCE;

    private final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private final HashMap<UUID, PlayerReplay> replays = new HashMap<>();
    private final HashMap<UUID, FinishedPlayerReplay> finishedReplays = new HashMap<>();
    private final HashMap<UUID, TaskRunnable<ReplayRunnable>> activeReplays = new HashMap<>();

    private ParkourReplayManager() {
    }

    public static ParkourReplayManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourReplayManager();
        return INSTANCE;
    }

    public void init() {
        scheduler.scheduleTask(() -> {
            for (Map.Entry<UUID, PlayerReplay> entry : replays.entrySet()) {
                Player player = connectionManager.getOnlinePlayerByUuid(entry.getKey());
                if (player == null) continue;

                PlayerReplay replay = entry.getValue();
                replay.pushFrame(new PlayerReplayFrame(
                        player.getPosition(),
                        player.getVelocity(),
                        player.isSprinting(),
                        player.getPose()
                ));
            }
        }, TaskSchedule.immediate(), TaskSchedule.tick(1));
    }

    public @Nullable PlayerReplay getParkourReplay(Player player) {
        return replays.get(player.getUuid());
    }

    public @Nullable FinishedPlayerReplay getFinishedReplay(UUID replayId) {
        return finishedReplays.get(replayId);
    }

    public void startTracking(Player player) {
        PlayerReplay replay = new PlayerReplay(player.getPosition());
        replays.put(player.getUuid(), replay);

        replay.startTracking();
    }

    public @Nullable UUID stopTracking(Player player) {
        PlayerReplay replay = getParkourReplay(player);
        if (replay == null) return null;

        replay.stopTracking();
        replays.remove(player.getUuid());

        UUID id = UUID.randomUUID();

        finishedReplays.put(id, new FinishedPlayerReplay(
                player.getUuid(),
                id,
                replay
        ));

        return id;
    }

    public boolean playReplay(Player player, FinishedPlayerReplay replay) {
        if (activeReplays.containsKey(player.getUuid())) return false;

        player.teleport(replay.startPosition().add(0, 5, 0));
        player.setAllowFlying(true);
        player.setFlying(true);
        player.setInvisible(true);

        ReplayRunnable runnable = new ReplayRunnable(player, replay, _ -> {

            TaskRunnable<ReplayRunnable> taskRunnable = activeReplays.remove(player.getUuid());
            if (taskRunnable != null) taskRunnable.task().cancel();

            player.setAllowFlying(false);
            player.setFlying(false);
            player.setInvisible(false);

        });

        Task task = scheduler.scheduleTask(runnable, TaskSchedule.immediate(), TaskSchedule.tick(1));
        activeReplays.put(player.getUuid(), new TaskRunnable<>(task, runnable));

        return true;
    }

    public boolean isWatchingReplay(Player player) {
        return activeReplays.containsKey(player.getUuid());
    }

    public void clearReplays(Player player) {
        TaskRunnable<ReplayRunnable> taskRunnable = activeReplays.remove(player.getUuid());
        if (taskRunnable != null) {
            taskRunnable.task().cancel();
            taskRunnable.runnable().cancel();
        }

        replays.remove(player.getUuid());
    }
}
