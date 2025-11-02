package nl.devpieter.features.economy.skills.category;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.awt.*;
import java.util.function.DoubleFunction;

public interface IExperienceCategory {

    String getId();

    Component getName();

    Color getColor();

    BossBar.Color getBossBarColor();

    double getBaseExperience();

    int getLevelsPerPrestige();

    DoubleFunction<Double> getLevelScaleFunction();

    DoubleFunction<Double> getPrestigeScaleFunction();

    default double getTotalExperienceFor(int prestige, int level) {
        double prestigeMultiplier = getPrestigeScaleFunction().apply(prestige);
        double levelCost = getLevelScaleFunction().apply(level);
        return (getBaseExperience() + levelCost) * prestigeMultiplier;
    }

    public static double getTotalExperienceUpTo(IExperienceCategory category, int prestige, int level) {
        double total = 0;
        for (int p = 0; p < prestige; p++) {
            for (int l = 0; l < category.getLevelsPerPrestige(); l++) {
                total += category.getTotalExperienceFor(p, l);
            }
        }
        for (int l = 0; l < level; l++) {
            total += category.getTotalExperienceFor(prestige, l);
        }
        return total;
    }

    public static LevelProgress getProgressFromTotalXP(IExperienceCategory category, double totalXP) {
        int prestige = 0;
        int level = 0;
        double xpLeft = totalXP;

        // Step 1: Find prestige
        while (true) {
            double prestigeXP = 0;
            for (int l = 0; l < category.getLevelsPerPrestige(); l++) {
                prestigeXP += category.getTotalExperienceFor(prestige, l);
            }

            if (xpLeft < prestigeXP)
                break;

            xpLeft -= prestigeXP;
            prestige++;
        }

        // Step 2: Find level within prestige
        while (true) {
            double levelXP = category.getTotalExperienceFor(prestige, level);
            if (xpLeft < levelXP)
                break;

            xpLeft -= levelXP;
            level++;
        }

        return new LevelProgress(prestige, level, xpLeft);
    }

    public record LevelProgress(int prestige, int level, double experience) {
    }

}
