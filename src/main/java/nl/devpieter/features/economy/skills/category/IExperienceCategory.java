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
}
