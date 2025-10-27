package nl.devpieter.features.experience.category;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import nl.devpieter.utils.TextUtils;

import java.util.function.DoubleFunction;

public class TestExperienceCategory implements IExperienceCategory {

    @Override
    public String getId() {
        return "test_experience_category";
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#65b4c9:#656cc9>Testing");
    }

    @Override
    public BossBar.Color getBossBarColor() {
        return BossBar.Color.BLUE;
    }

    @Override
    public double getBaseExperience() {
        return 10.0;
    }

    @Override
    public int getLevelsPerPrestige() {
        return 20;
    }

    @Override
    public DoubleFunction<Double> getLevelScaleFunction() {
        return level -> 10 + Math.pow(level, 2) * 5;
    }

    @Override
    public DoubleFunction<Double> getPrestigeScaleFunction() {
        return prestige -> Math.pow(1.5, prestige);
    }
}
