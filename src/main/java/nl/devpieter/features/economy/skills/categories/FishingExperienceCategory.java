package nl.devpieter.features.economy.skills.categories;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import nl.devpieter.features.economy.skills.category.IExperienceCategory;
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
        Component hoverText = TextUtils.combine(
                TextUtils.miniText("<gradient:#65b4c9:#656cc9><bold>Fishing"),
                Component.newline(),
                TextUtils.miniText("<gray><i>“Patience is the fisher’s true weapon.”"),
                Component.newline(),
                Component.newline(),
                TextUtils.miniText("<#ffffff>• <#86c2d9>Earn experience by fishing in any body of water."),
                Component.newline(),
                TextUtils.miniText("<#ffffff>• <#86c2d9>Rarer catches grant greater experience.")
        );

        return TextUtils
                .miniText("<gradient:#65b4c9:#656cc9>Fishing")
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
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
