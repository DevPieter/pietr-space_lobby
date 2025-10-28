package nl.devpieter.features.experience.categories;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import nl.devpieter.features.experience.category.IExperienceCategory;
import nl.devpieter.utils.minestom.TextUtils;

import java.awt.*;
import java.util.function.DoubleFunction;

public class FishingExperienceCategory implements IExperienceCategory {

    @Override
    public String getId() {
        return "fishing";
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#65b4c9:#656cc9>Fishing");
    }

    @Override
    public Color getColor() {
        return new Color(0x65b4c9);
    }

    @Override
    public BossBar.Color getBossBarColor() {
        return BossBar.Color.BLUE;
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
