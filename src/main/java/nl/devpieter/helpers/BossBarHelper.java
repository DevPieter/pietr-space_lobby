package nl.devpieter.helpers;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BossBarHelper {

    private static BossBarHelper INSTANCE;

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private BossBarHelper() {
    }

    public static BossBarHelper getInstance() {
        if (INSTANCE == null) INSTANCE = new BossBarHelper();
        return INSTANCE;
    }

    record BossBarHolder(
            String id,
            Task hideTask,
            BossBar bossBar
    ) {
    }


    private final HashMap<UUID, List<BossBarHolder>> bossBars = new HashMap<>();

    public void showTempBossBar(Player player, String id, BossBar bossBar, Duration duration) {
        List<BossBarHolder> playerBossBars = bossBars.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>());

        BossBarHolder existing = getBossBar(player, id);
        if (existing != null) {
            player.hideBossBar(existing.bossBar);
            existing.hideTask.cancel();

            playerBossBars.remove(existing);
        }

        player.showBossBar(bossBar);

        Task hideTask = scheduler.buildTask(() -> {
            player.hideBossBar(bossBar);
            playerBossBars.removeIf(bossBarHolder -> bossBarHolder.id().equals(id));
        }).delay(TaskSchedule.duration(duration)).schedule();

        BossBarHolder bossBarHolder = new BossBarHolder(id, hideTask, bossBar);
        playerBossBars.add(bossBarHolder);
    }

    private @Nullable BossBarHolder getBossBar(Player player, String bossBarId) {
        List<BossBarHolder> playerBossBars = bossBars.get(player.getUuid());
        if (playerBossBars == null) return null;

        for (BossBarHolder bossBarHolder : playerBossBars) {
            if (!bossBarHolder.id().equals(bossBarId)) continue;
            return bossBarHolder;
        }

        return null;
    }
}
