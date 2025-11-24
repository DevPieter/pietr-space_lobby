package nl.devpieter.features.mining.models;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.blockHandlers.MinableBlockHandler;
import nl.devpieter.features.mining.enums.ToolType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MiningStep(
        Block block,
        List<ToolType> toolTypes,
        double hardness,
        int extraRevertTicks,
        int baseBreakTicks
) {
    public MiningStep(Block block) {
        this(block, List.of(), -1, 0, -1);
    }

    public MiningStep(Block block, ToolType toolType, double hardness, int baseBreakTicks) {
        this(block, List.of(toolType), hardness, 0, baseBreakTicks);
    }

    public MiningStep(Block block, ToolType toolType, double hardness, int extraRevertTicks, int baseBreakTicks) {
        this(block, List.of(toolType), hardness, extraRevertTicks, baseBreakTicks);
    }

    public @Nullable MinableBlockHandler blockHandler() {
        if (hardness <= 0 || baseBreakTicks <= 0) return null;
        return new MinableBlockHandler(hardness, baseBreakTicks, toolTypes);
    }
}
