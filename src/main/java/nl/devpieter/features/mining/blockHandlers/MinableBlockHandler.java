package nl.devpieter.features.mining.blockHandlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public record MinableBlockHandler(double hardness, int totalBreakTicks) implements BlockHandler {

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "minable_block");
    }
}
