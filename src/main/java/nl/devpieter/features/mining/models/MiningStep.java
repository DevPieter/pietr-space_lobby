package nl.devpieter.features.mining.models;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.blockHandlers.MinableBlockHandler;
import org.jetbrains.annotations.Nullable;

public record MiningStep(
        Block block,
        double hardness,
        int extraRevertTicks,
        int baseBreakTicks
) {

    public MiningStep(Block block) {
        this(block, -1, 0, -1);
    }

    public MiningStep(Block block, double hardness, int baseBreakTicks) {
        this(block, hardness, 0, baseBreakTicks);
    }

    public @Nullable MinableBlockHandler blockHandler() {
        if (hardness <= 0 || baseBreakTicks <= 0) return null;
        return new MinableBlockHandler(hardness, baseBreakTicks);
    }
}
