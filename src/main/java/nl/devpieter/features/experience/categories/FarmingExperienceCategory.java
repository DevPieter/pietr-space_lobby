package nl.devpieter.features.experience.categories;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import nl.devpieter.features.experience.category.IExperienceCategory;
import nl.devpieter.utils.minestom.TextUtils;

import java.awt.*;
import java.util.function.DoubleFunction;

public class FarmingExperienceCategory implements IExperienceCategory {

    @Override
    public String getId() {
        return "farming";
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#c9d265:#c9a665>Farming");
    }

    @Override
    public Color getColor() {
        return new Color(0xc9d265);
    }

    @Override
    public BossBar.Color getBossBarColor() {
        return BossBar.Color.YELLOW;
    }

    @Override
    public double getBaseExperience() {
        return 20;
    }

    @Override
    public int getLevelsPerPrestige() {
        return 20;
    }

    @Override
    public DoubleFunction<Double> getLevelScaleFunction() {
        // f(x) = 5 + (x^1.5) * 2
        return level -> 5 + Math.pow(level, 1.5) * 2;
    }

    @Override
    public DoubleFunction<Double> getPrestigeScaleFunction() {
        // f(x) = 1.20^x
        return prestige -> Math.pow(1.20, prestige);
    }
}
