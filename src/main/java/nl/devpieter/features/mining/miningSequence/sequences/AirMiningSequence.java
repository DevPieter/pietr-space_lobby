package nl.devpieter.features.mining.miningSequence.sequences;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.MiningSteps;
import nl.devpieter.features.mining.miningSequence.MiningSequence;
import nl.devpieter.features.mining.models.MiningStep;

import java.util.List;

public class AirMiningSequence extends MiningSequence {

    @Override
    public int getRevertTicks() {
        return 12 * 20;
    }

    @Override
    public List<MiningStep> getSteps() {
        return List.of(
                MiningSteps.CRACKED_STONE,
                new MiningStep(Block.AIR)
        );
    }
}
