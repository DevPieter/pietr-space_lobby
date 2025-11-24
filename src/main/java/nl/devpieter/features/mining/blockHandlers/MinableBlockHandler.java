package nl.devpieter.features.mining.blockHandlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import nl.devpieter.features.mining.enums.ToolType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MinableBlockHandler(
        double hardness,
        int baseBreakTicks,
        List<ToolType> toolTypes
) implements BlockHandler {

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "minable_block");
    }
}
