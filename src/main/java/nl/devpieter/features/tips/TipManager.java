package nl.devpieter.features.tips;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import nl.devpieter.PlayLogger;
import nl.devpieter.features.tips.models.TipHolder;
import nl.devpieter.utils.minestom.SoundUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TipManager {

    private static TipManager INSTANCE;

    private static final String DEFAULT_TIP_PREFIX = "TIP";
    private static final Duration TIP_COOLDOWN = Duration.ofMinutes(20);

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();
    private final PlayLogger playLogger = PlayLogger.getInstance();

    private final HashMap<UUID, List<TipHolder>> tips = new HashMap<>();
    private final HashMap<UUID, List<String>> receivedTips = new HashMap<>();

    private final List<UUID> tipsDisabled = new ArrayList<>();
    private final HashMap<UUID, List<String>> ignoredTips = new HashMap<>();
    private final HashMap<UUID, List<String>> completedTips = new HashMap<>();

    private TipManager() {
    }

    public static TipManager getInstance() {
        if (INSTANCE == null) INSTANCE = new TipManager();
        return INSTANCE;
    }

    public void showTipLater(@NotNull Player player, @NotNull String id, Duration delay, @NotNull String message) {
        showTipLater(player, id, delay, DEFAULT_TIP_PREFIX, message);
    }

    public void showTipLater(@NotNull Player player, @NotNull String id, Duration delay, @NotNull String prefix, @NotNull String message) {
        showTipLater(player, id, delay, prefix, message, true);
    }

    public void showTipLater(@NotNull Player player, @NotNull String id, Duration delay, @NotNull String prefix, @NotNull String message, boolean sound) {
        scheduler.buildTask(() -> showTip(player, id, prefix, message, true)).delay(delay).schedule();
    }

    public void showTip(@NotNull Player player, @NotNull String id, @NotNull String message) {
        showTip(player, id, DEFAULT_TIP_PREFIX, message);
    }

    public void showTip(@NotNull Player player, @NotNull String id, @NotNull String prefix, @NotNull String message) {
        showTip(player, id, prefix, message, true);
    }

    public void showTip(@NotNull Player player, @NotNull String id, @NotNull String prefix, @NotNull String message, boolean sound) {
        showTip(player, id, TextUtils.combine(
                TextUtils.miniText("<#e8e827><b>["),
                TextUtils.miniText("<#e8e827><b>%s", prefix),
                TextUtils.miniText("<#e8e827><b>]"),
                Component.space(),
                TextUtils.miniText("<#f0f097>%s", message)
        ), sound);
    }

    public void showTip(@NotNull Player player, @NotNull String id, @NotNull Component message, boolean sound) {
        removeExpiredTips(player);

        if (!canShowTip(player, id)) {
            playLogger.debug(player, true, "Tip {} not shown (conditions not met)", id);
            return;
        }

        List<TipHolder> tipHolders = tips.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>());
        tipHolders.add(new TipHolder(id, System.currentTimeMillis()));

        receivedTips.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>()).add(id);

        Component ignore = TextUtils.miniText("<gray>[Ignore]</gray>")
                .clickEvent(ClickEvent.runCommand(String.format("/tip ignore %s true", id)));

        player.sendMessage(TextUtils.combine(
                message,
                Component.space(),
                ignore
        ));

        if (sound) playSoundEffect(player);
        playLogger.debug(player, true, "Tip {} shown (sound: {})", id, sound);
    }

    public void disableTips(@NotNull Player player, boolean disable) {
        if (disable) {
            if (!tipsDisabled.contains(player.getUuid())) {
                tipsDisabled.add(player.getUuid());
                playLogger.debug(player, true, "Disabled tips");
            }
        } else {
            tipsDisabled.remove(player.getUuid());
            playLogger.debug(player, true, "Enabled tips");
        }
    }

    public boolean areTipsDisabled(@NotNull Player player) {
        return tipsDisabled.contains(player.getUuid());
    }

    public boolean ignoreTip(@NotNull Player player, @NotNull String tipId) {
        List<String> received = receivedTips.get(player.getUuid());
        if (received == null || !received.contains(tipId)) return false;

        List<String> ignored = ignoredTips.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>());
        if (ignored.contains(tipId)) return true;

        ignored.add(tipId);
        playLogger.debug(player, true, "Ignored tip {}", tipId);

        return true;
    }

    public boolean isTipIgnored(@NotNull Player player, @NotNull String tipId) {
        List<String> ignored = ignoredTips.get(player.getUuid());
        if (ignored == null) return false;

        return ignored.contains(tipId);
    }

    public void completeTip(@NotNull Player player, @NotNull String tipId) {
        List<String> completed = completedTips.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>());
        if (completed.contains(tipId)) return;

        completed.add(tipId);
        playLogger.debug(player, true, "Completed tip {}", tipId);
    }

    public boolean isTipCompleted(@NotNull Player player, @NotNull String tipId) {
        List<String> completed = completedTips.get(player.getUuid());
        if (completed == null) return false;

        return completed.contains(tipId);
    }

    public void resetTips(@NotNull Player player) {
        receivedTips.remove(player.getUuid());
        tips.remove(player.getUuid());
        playLogger.debug(player, true, "Reset tips");
    }

    private boolean canShowTip(@NotNull Player player, @NotNull String tipId) {
        if (areTipsDisabled(player)) return false;
        if (isTipIgnored(player, tipId)) return false;
        if (isTipCompleted(player, tipId)) return false;

        TipHolder existing = getTip(player, tipId);
        if (existing == null) return true;

        long elapsedTime = System.currentTimeMillis() - existing.timestamp();
        return elapsedTime >= TIP_COOLDOWN.toMillis();
    }

    private @Nullable TipHolder getTip(@NotNull Player player, @NotNull String tipId) {
        List<TipHolder> tipHolders = tips.get(player.getUuid());
        if (tipHolders == null) return null;

        for (TipHolder tipHolder : tipHolders) {
            if (!tipHolder.id().equals(tipId)) continue;
            return tipHolder;
        }

        return null;
    }

    private void removeExpiredTips(@NotNull Player player) {
        List<TipHolder> tipHolders = tips.get(player.getUuid());
        if (tipHolders == null) return;

        long currentTime = System.currentTimeMillis();
        tipHolders.removeIf(tipHolder -> (currentTime - tipHolder.timestamp()) >= TIP_COOLDOWN.toMillis());
    }

    private void playSoundEffect(Player player) {
        SoundUtils.playSoundForPlayer(player, SoundEvent.BLOCK_NOTE_BLOCK_HARP, 0.4f, 1.2f);

        scheduler.buildTask(() -> SoundUtils.playSoundForPlayer(player, SoundEvent.BLOCK_NOTE_BLOCK_HARP, 0.4f, 1.6f))
                .delay(Duration.ofMillis(120))
                .schedule();

        scheduler.buildTask(() -> SoundUtils.playSoundForPlayer(player, SoundEvent.ENTITY_VILLAGER_YES, 0.4f, 1.2f))
                .delay(Duration.ofMillis(260))
                .schedule();
    }
}
