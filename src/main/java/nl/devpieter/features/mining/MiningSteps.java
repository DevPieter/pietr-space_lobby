package nl.devpieter.features.mining;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.models.MiningStep;

public class MiningSteps {

    public static final MiningStep FINAL_STEP = new MiningStep(Block.BEDROCK);

    /* Basic Blocks */
    public static final MiningStep STONE = new MiningStep(Block.STONE, 1, 10);
    public static final MiningStep COBBLESTONE = new MiningStep(Block.COBBLESTONE, 1, 20);

    /* Ores */
    public static final MiningStep COAL_ORE = new MiningStep(Block.COAL_ORE, 1, 30);
    public static final MiningStep IRON_ORE = new MiningStep(Block.IRON_ORE, 2, 30, 40);
    public static final MiningStep GOLD_ORE = new MiningStep(Block.GOLD_ORE, 2, 30, 30);
    public static final MiningStep DIAMOND_ORE = new MiningStep(Block.DIAMOND_ORE, 3, 80, 60);
    public static final MiningStep EMERALD_ORE = new MiningStep(Block.EMERALD_ORE, 3, 80, 80);

    /* Special Blocks */

//    public static final MiningStep MITHRIL_OUTER_1 = new MiningStep(Block.GRAY_WOOL, 2, 50, 50);
//    public static final MiningStep MITHRIL_OUTER_2 = new MiningStep(Block.LIGHT_GRAY_WOOL, 2, 50, 50);
//    public static final MiningStep MITHRIL_INNER = new MiningStep(Block.ANCIENT_DEBRIS, 3, 70, 70);
//    public static final MiningStep MITHRIL_CORE = new MiningStep(Block.NETHERITE_BLOCK, 4, 100, 100);
}
