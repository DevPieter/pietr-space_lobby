package nl.devpieter.features.experience.category;

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
        double prestigeBonus = getPrestigeScaleFunction().apply(prestige);
        double levelCost = getLevelScaleFunction().apply(level);
        return getBaseExperience() + prestigeBonus + levelCost;
    }
}
