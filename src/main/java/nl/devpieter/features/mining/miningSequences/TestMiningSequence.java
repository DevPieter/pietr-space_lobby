package nl.devpieter.features.mining.miningSequences;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.blockHandlers.MinableBlockHandler;
import nl.devpieter.features.mining.miningSequence.MiningSequence;
import nl.devpieter.features.mining.models.MiningStep;

import java.util.List;

public class TestMiningSequence extends MiningSequence {

    @Override
    public int getRevertTicks() {
        return 6 * 20;
    }

    @Override
    public List<MiningStep> getSteps() {
        return List.of(
                new MiningStep(
                        Block.STONE,
                        new MinableBlockHandler(1, 20)
                ),
                new MiningStep(
                        Block.COBBLESTONE,
                        new MinableBlockHandler(1, 20 * 2)
                ),
                new MiningStep(
                        Block.COAL_ORE,
                        new MinableBlockHandler(1, 20 * 3)
                ),
                new MiningStep(
                        Block.BEDROCK,
                        null
                )
        );
    }
}
