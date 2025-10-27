package nl.devpieter.features.experience.models;

import nl.devpieter.features.experience.category.IExperienceCategory;

import java.util.UUID;

public class LevelHolder {

    public UUID playerId;
    private String categoryId;

    private int prestige;
    private int level;
    private double experience;

    public LevelHolder(UUID playerId, String categoryId) {
        this.playerId = playerId;
        this.categoryId = categoryId;
        this.prestige = 0;
        this.level = 0;
        this.experience = 0.0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public int getPrestige() {
        return prestige;
    }

    public int getLevel() {
        return level;
    }

    public double getExperience() {
        return experience;
    }

    public void addExperience(double amount, IExperienceCategory category) {
        experience += amount;
        checkLevelUp(category);
    }

    private void checkLevelUp(IExperienceCategory category) {
        while (experience >= getRequiredExperienceForNextLevel(category)) {
            experience -= getRequiredExperienceForNextLevel(category);
            level++;

            if (level >= category.getLevelsPerPrestige()) {
                level = 0;
                prestige++;
            }
        }
    }

    public double getRequiredExperienceForNextLevel(IExperienceCategory category) {
        double levelScale = category.getLevelScaleFunction().apply(level);
        double prestigeMultiplier = category.getPrestigeScaleFunction().apply(prestige);
        return category.getBaseExperience() + (levelScale * prestigeMultiplier);
    }
}
