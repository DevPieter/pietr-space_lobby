package nl.devpieter.features.mining.tool;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;

import java.util.UUID;

public interface ITool {

    UUID getId();

    Component getName();

    ItemStack buildItem();

    double getMiningStrength();

    double getMiningSpeedMultiplier();
}
