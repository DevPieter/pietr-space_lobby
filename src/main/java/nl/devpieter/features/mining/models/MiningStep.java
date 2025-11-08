package nl.devpieter.features.mining.models;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.blockHandlers.MinableBlockHandler;
import org.jetbrains.annotations.Nullable;

public record MiningStep(Block block, @Nullable MinableBlockHandler blockHandler) {
}
