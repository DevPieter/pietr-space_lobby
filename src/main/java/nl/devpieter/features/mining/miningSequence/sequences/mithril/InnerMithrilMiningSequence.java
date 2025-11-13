package nl.devpieter.features.mining.miningSequence.sequences.mithril;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.MiningSteps;
import nl.devpieter.features.mining.miningSequence.MiningSequence;
import nl.devpieter.features.mining.models.MiningStep;

import java.util.List;

public class InnerMithrilMiningSequence extends MiningSequence {

    private final List<MiningStep> possibleSteps = List.of(
            new MiningStep(Block.PRISMARINE, 3, 70, 70),
            new MiningStep(Block.PRISMARINE_BRICKS, 3, 70, 70),
            new MiningStep(Block.DARK_PRISMARINE, 3, 70, 70)
    );

    @Override
    public int getRevertTicks() {
        return 6 * 20;
    }

    @Override
    public List<MiningStep> getSteps() {
        return List.of(
                getRandomStep(),
                MiningSteps.COBBLESTONE,
                MiningSteps.FINAL_STEP
        );
    }

    private MiningStep getRandomStep() {
        int index = (int) (Math.random() * possibleSteps.size());
        return possibleSteps.get(index);
    }
}
