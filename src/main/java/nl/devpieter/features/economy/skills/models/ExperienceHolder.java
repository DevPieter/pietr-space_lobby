package nl.devpieter.features.economy.skills.models;

import nl.devpieter.features.economy.skills.SkillManager;
import nl.devpieter.features.economy.skills.category.IExperienceCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ExperienceHolder {

    private final SkillManager skillManager = SkillManager.getInstance();

    private final UUID playerId;
    private final String categoryId;

    private double totalExperienceGained;
    private double totalExperienceRevoked;

    private boolean isDirty = true;
    private @Nullable ExperienceCalculationResult cachedResult;

    public ExperienceHolder(UUID playerId, String categoryId) {
        this.playerId = playerId;
        this.categoryId = categoryId;
    }

    public UUID playerId() {
        return playerId;
    }

    public String categoryId() {
        return categoryId;
    }

    public double totalExperienceGained() {
        return totalExperienceGained;
    }

    public double totalExperienceRevoked() {
        return totalExperienceRevoked;
    }

    public double totalExperience() {
        return Math.max(0, totalExperienceGained - totalExperienceRevoked);
    }

    public double totalExperienceRaw() {
        return totalExperienceGained - totalExperienceRevoked;
    }

    public double cachedExperience() {
        return cachedResult == null ? -1 : cachedResult.experience();
    }

    public int cachedLevel() {
        return cachedResult == null ? -1 : cachedResult.level();
    }

    public int cachedPrestige() {
        return cachedResult == null ? -1 : cachedResult.prestige();
    }

    public double cachedProgressPercentage() {
        return cachedResult == null ? -1 : cachedResult.progressPercentage();
    }

    public void grantExperience(double amount) {
        if (amount <= 0) return;

        totalExperienceGained += amount;
        isDirty = true;
    }

    public void revokeExperience(double amount) {
        if (amount <= 0) return;

        totalExperienceRevoked += amount;
        isDirty = true;
    }

    public void updateCache() {
        updateCache(false);
    }

    public void updateCache(boolean force) {
        if (!isDirty && !force) return;

        IExperienceCategory category = skillManager.getCategory(categoryId);
        if (category == null) return;

        cachedResult = calculate(category, this);
        isDirty = false;
    }

    public static @NotNull ExperienceCalculationResult calculate(@NotNull IExperienceCategory category, @NotNull ExperienceHolder holder) {
        double xpLeft = holder.totalExperience();

        int prestige = 0;
        int level = 0;

        // Find prestige
        while (true) {
            double prestigeXP = 0;

            for (int l = 0; l < category.getLevelsPerPrestige(); l++) {
                prestigeXP += getTotalExperienceFor(category, prestige, l);
            }

            if (xpLeft < prestigeXP) break;

            xpLeft -= prestigeXP;
            prestige++;
        }

        // Find level within prestige
        while (true) {
            double levelXP = getTotalExperienceFor(category, prestige, level);
            if (xpLeft < levelXP) break;

            xpLeft -= levelXP;
            level++;
        }

        double xpForNextLevel = getTotalExperienceFor(category, prestige, level + 1);
        double progress = xpForNextLevel == 0 ? 0 : (xpLeft / xpForNextLevel) * 100.0;

        return new ExperienceCalculationResult(xpLeft, level, prestige, progress);
    }

    private static double getTotalExperienceFor(IExperienceCategory category, int prestige, int level) {
        double prestigeMultiplier = category.getPrestigeScaleFunction().apply(prestige);
        double levelCost = category.getLevelScaleFunction().apply(level);
        return (category.getBaseExperience() + levelCost) * prestigeMultiplier;
    }
}
