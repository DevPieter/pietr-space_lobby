package nl.devpieter.features.experience;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.features.experience.categories.FarmingExperienceCategory;
import nl.devpieter.features.experience.categories.FishingExperienceCategory;
import nl.devpieter.features.experience.category.IExperienceCategory;
import nl.devpieter.features.experience.category.TestExperienceCategory;
import nl.devpieter.features.experience.models.LevelHolder;
import nl.devpieter.utils.SoundUtils;
import nl.devpieter.utils.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExperienceManager {

    private static ExperienceManager INSTANCE;

    private final List<IExperienceCategory> categories = new ArrayList<>();

    private final HashMap<UUID, LevelHolder> levels = new HashMap<>();

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

    private BossBar bossBar;

    public void grantExperience(Player player, String categoryId, double amount) {
        IExperienceCategory category = getCategoryById(categoryId);
        if (category == null) return;

        LevelHolder levelHolder = levels.computeIfAbsent(player.getUuid(), id -> new LevelHolder(id, categoryId));
        int currentLevel = levelHolder.getLevel();
        levelHolder.addExperience(amount, category);
        int newLevel = levelHolder.getLevel();

        if (newLevel > currentLevel) {
            player.sendMessage(TextUtils.miniText("<green>You leveled up to Level <b>" + newLevel + "</b> in ").append(category.getName()).append(TextUtils.miniText("<green>!")));
            SoundUtils.playSoundForPlayer(player, SoundEvent.ENTITY_PLAYER_LEVELUP);
        }

        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }

        bossBar = BossBar.bossBar(
                category.getName().append(TextUtils.miniText(" Level " + levelHolder.getLevel())),
                (float) (levelHolder.getExperience() / levelHolder.getRequiredExperienceForNextLevel(category)),
                category.getBossBarColor(),
                BossBar.Overlay.PROGRESS
        );
        player.showBossBar(bossBar);

        player.sendActionBar(TextUtils.miniText("<green>+" + String.format("%.2f", amount) + " ").append(category.getName()).append(TextUtils.miniText(" <green>XP!")));

//        double currentExp = levelHolder.getExperience();
//        double neededExp = levelHolder.getRequiredExperienceForNextLevel(category);

//        double percent = (currentExp / neededExp) * 100;

//        player.sendMessage("You gained " + amount + " XP in " + category.getName() + "! (" + String.format("%.2f", percent) + "% to next level)");
//        player.sendMessage(TextUtils.miniText("<green>You gained <white>" + String.format("%.2f", amount) + " XP <green>in <white>").append(category.getName()).append(TextUtils.miniText("<green>! (<white>" + String.format("%.2f", percent) + "% <green>to next level)")));
    }

    public IExperienceCategory getCategoryById(String categoryId) {
        for (IExperienceCategory category : categories) {
            if (!category.getId().equals(categoryId)) continue;
            return category;
        }

        return null;
    }
}
