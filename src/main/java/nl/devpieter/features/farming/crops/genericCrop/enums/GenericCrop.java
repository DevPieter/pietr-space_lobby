package nl.devpieter.features.farming.crops.genericCrop.enums;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public enum GenericCrop {
    WHEAT(Block.WHEAT, 7),
    CARROTS(Block.CARROTS, 7),
    POTATOES(Block.POTATOES, 7),
    BEETROOTS(Block.BEETROOTS, 3);

    private final Block block;
    private final int maxGrowthStage;

    GenericCrop(Block block, int maxGrowthStage) {
        this.block = block;
        this.maxGrowthStage = maxGrowthStage;
    }

    public Block getBlock() {
        return block;
    }

    public int getMaxGrowthStage() {
        return maxGrowthStage;
    }

    public static @Nullable GenericCrop fromName(String name) {
        for (GenericCrop crop : values()) {
            if (crop.name().equalsIgnoreCase(name)) return crop;
        }

        return null;
    }
}
