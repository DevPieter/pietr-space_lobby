package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class DoorBlockHandler implements BlockHandler {

    @Override
    public boolean onInteract(Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point point = interaction.getBlockPosition();
        Block block = interaction.getBlock();

        boolean isOpen = "true".equals(block.getProperty("open"));
        boolean isUpperPart = "upper".equals(block.getProperty("half"));
        String newState = isOpen ? "false" : "true";

        Point lowerPos = isUpperPart ? point.withY(point.y() - 1) : point;
        Point upperPos = isUpperPart ? point : point.withY(point.y() + 1);

        Block lower = instance.getBlock(lowerPos);
        Block upper = instance.getBlock(upperPos);

        Block newLower = lower.withProperty("open", newState);
        Block newUpper = upper.withProperty("open", newState);

        instance.setBlock(lowerPos, newLower);
        instance.setBlock(upperPos, newUpper);

        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "door_block");
    }
}
