package nl.devpieter.features.experience;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.features.experience.categories.FarmingExperienceCategory;
import nl.devpieter.features.experience.categories.FishingExperienceCategory;
import nl.devpieter.features.experience.category.IExperienceCategory;
import nl.devpieter.features.experience.category.TestExperienceCategory;
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

//    private BossBar bossBar;

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

        if (newLevel > currentLevel) {
            player.sendMessage(TextUtils.miniText("<green>You leveled up to Level <b>" + newLevel + "</b> in ").append(category.getName()).append(TextUtils.miniText("<green>!")));
            SoundUtils.playSoundForPlayer(player, SoundEvent.ENTITY_PLAYER_LEVELUP);
        }

        BossBar bossBar = bossBars.get(player.getUuid() + "_" + categoryId);
        if (bossBar != null) player.hideBossBar(bossBar);

        bossBar = BossBar.bossBar(
                category.getName().append(TextUtils.miniText(" Level " + levelHolder.getLevel())),
                (float) (levelHolder.getExperience() / levelHolder.getRequiredExperienceForNextLevel(category)),
                category.getBossBarColor(),
                BossBar.Overlay.PROGRESS
        );

        bossBars.put(player.getUuid() + "_" + categoryId, bossBar);
        player.showBossBar(bossBar);

        Color plusColor = category.getColor();
        String plusHex = TextColor.color(plusColor.getRGB()).asHexString();

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
