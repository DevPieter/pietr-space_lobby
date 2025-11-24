package nl.devpieter.features.mining;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.enums.ToolType;
import nl.devpieter.features.mining.models.MiningStep;

public class MiningSteps {

    public static final MiningStep FINAL_STEP = new MiningStep(Block.BEDROCK);

    /* Basic Blocks */
    public static final MiningStep STONE = new MiningStep(Block.STONE, ToolType.PICKAXE, 1, 15);
    public static final MiningStep CRACKED_STONE = new MiningStep(Block.CRACKED_STONE_BRICKS, ToolType.PICKAXE, 1, 10);
    public static final MiningStep COBBLESTONE = new MiningStep(Block.COBBLESTONE, ToolType.PICKAXE, 1, 20);

    /* Ores */
    public static final MiningStep COAL_ORE = new MiningStep(Block.COAL_ORE, ToolType.PICKAXE, 1, 30);
    public static final MiningStep IRON_ORE = new MiningStep(Block.IRON_ORE, ToolType.PICKAXE, 2, 30, 40);
    public static final MiningStep GOLD_ORE = new MiningStep(Block.GOLD_ORE, ToolType.PICKAXE, 2, 30, 30);
    public static final MiningStep DIAMOND_ORE = new MiningStep(Block.DIAMOND_ORE, ToolType.PICKAXE, 3, 80, 60);
    public static final MiningStep EMERALD_ORE = new MiningStep(Block.EMERALD_ORE, ToolType.PICKAXE, 3, 80, 80);
}
