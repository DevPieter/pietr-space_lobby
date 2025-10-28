package nl.devpieter.features.experience;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.features.experience.categories.FarmingExperienceCategory;
import nl.devpieter.features.experience.categories.FishingExperienceCategory;
import nl.devpieter.features.experience.categories.TestExperienceCategory;
import nl.devpieter.features.experience.category.IExperienceCategory;
import nl.devpieter.features.experience.models.LevelHolder;
import nl.devpieter.utils.minestom.SoundUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExperienceManager {

    private static ExperienceManager INSTANCE;

    private final List<IExperienceCategory> categories = new ArrayList<>();

    private final HashMap<UUID, LevelHolder> levels = new HashMap<>();
    private final HashMap<String, BossBar> bossBars = new HashMap<>();
    private final HashMap<String, Task> hideTasks = new HashMap<>();

    private ExperienceManager() {
        categories.addAll(List.of(
                new TestExperienceCategory(),
                new FarmingExperienceCategory(),
                new FishingExperienceCategory()
        ));
    }

    public static ExperienceManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ExperienceManager();
        return INSTANCE;
    }

    public void grantExperience(Player player, String categoryId, double amount) {
        grantExperience(player, categoryId, amount, 0);
    }

    public void grantExperience(Player player, String categoryId, double amount, double bonus) {
        IExperienceCategory category = getCategoryById(categoryId);
        if (category == null) return;

        LevelHolder levelHolder = levels.computeIfAbsent(player.getUuid(), id -> new LevelHolder(id, categoryId));

        int currentLevel = levelHolder.getLevel();
        levelHolder.addExperience(amount + bonus, category);
        int newLevel = levelHolder.getLevel();

        Color plusColor = category.getColor();
        String plusHex = TextColor.color(plusColor.getRGB()).asHexString();

        if (newLevel > currentLevel) {
            Component message = TextUtils.combine(
                    category.getName(),
                    Component.space(),
                    TextUtils.miniText("<#576272>levelled up to"),
                    Component.space(),
                    TextUtils.miniText("<%s>Level %d!</%s>", plusHex, newLevel, plusHex)
            );

            player.sendMessage(message);
            SoundUtils.playSoundForPlayer(player, SoundEvent.ENTITY_PLAYER_LEVELUP);
        }

        BossBar existingBossBar = bossBars.get(player.getUuid() + "_" + categoryId);
        if (existingBossBar != null) {
            player.hideBossBar(existingBossBar);
            bossBars.remove(player.getUuid() + "_" + categoryId);
        }

        Task exitTask = hideTasks.get(player.getUuid() + "_" + categoryId);
        if (exitTask != null) {
            exitTask.cancel();
            hideTasks.remove(player.getUuid() + "_" + categoryId);
        }

        BossBar bossBar = BossBar.bossBar(
                category.getName().append(TextUtils.miniText(" Level " + levelHolder.getLevel())),
                (float) (levelHolder.getExperience() / levelHolder.getTotalRequiredExperienceForNextLevel(category)),
                category.getBossBarColor(),
                BossBar.Overlay.PROGRESS
        );

        bossBars.put(player.getUuid() + "_" + categoryId, bossBar);
        player.showBossBar(bossBar);

        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        Task hideTask = scheduler.buildTask(() -> {
            player.hideBossBar(bossBar);
            bossBars.remove(player.getUuid() + "_" + categoryId);
            hideTasks.remove(player.getUuid() + "_" + categoryId);
        }).delay(TaskSchedule.millis(4000)).schedule();

        hideTasks.put(player.getUuid() + "_" + categoryId, hideTask);

        if (bonus > 0) {
            Component message = TextUtils.combine(
                    TextUtils.miniText("<#576272><%s>+</%s>%s", plusHex, plusHex, formatDouble(amount)),
                    Component.space(),
                    category.getName(),
                    Component.space(),
                    TextUtils.miniText("<#576272>and"),
                    Component.space(),
                    TextUtils.miniText("<#576272><#FFD700>+</#FFD700>%s", formatDouble(bonus)),
                    Component.space(),
                    TextUtils.miniText("<gradient:#FFD700:#FFA500>Bonus</gradient> <#576272>XP!")
            );

            player.sendActionBar(message);
        } else {
            Component message = TextUtils.combine(
                    TextUtils.miniText("<#576272><%s>+</%s>%s", plusHex, plusHex, formatDouble(amount)),
                    Component.space(),
                    category.getName(),
                    Component.space(),
                    TextUtils.miniText("<#576272>XP!")
            );

            player.sendActionBar(message);
        }

        IExperienceCategory.LevelProgress progress = IExperienceCategory.getProgressFromTotalXP(category, 10_000);
        System.out.println("DEBUG: " + categoryId + " - Prestige: " + progress.prestige() + ", Level: " + progress.level() + ", XP: " + formatDouble(progress.experience()));

        double neededXp = IExperienceCategory.getTotalExperienceUpTo(category, 1, 0);
        System.out.println("DEBUG: " + categoryId + ": " + formatDouble(neededXp));
    }

    public IExperienceCategory getCategoryById(String categoryId) {
        for (IExperienceCategory category : categories) {
            if (!category.getId().equals(categoryId)) continue;
            return category;
        }

        return null;
    }

    private @NotNull String formatDouble(double value) {
        if (value % 1 == 0) return String.format("%.0f", value);
        else return String.format("%.2f", value);
    }
}
