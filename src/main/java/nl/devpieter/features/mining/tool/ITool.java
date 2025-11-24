package nl.devpieter.features.mining.tool;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import nl.devpieter.features.mining.enums.ToolType;

import java.util.List;
import java.util.UUID;

public interface ITool {

    UUID getId();

    Component getName();

    List<ToolType> getToolTypes();

    ItemStack buildItem();

    double getMiningStrength();

    double getMiningSpeedMultiplier();
}
