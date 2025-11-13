package nl.devpieter.features.mining.miningSequence.sequences.mithril;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.MiningSteps;
import nl.devpieter.features.mining.miningSequence.MiningSequence;
import nl.devpieter.features.mining.models.MiningStep;

import java.util.List;

public class CoreMithrilMiningSequence extends MiningSequence {

    private final List<MiningStep> possibleSteps = List.of(
            new MiningStep(Block.LIGHT_BLUE_WOOL, 4, 100, 100),
            new MiningStep(Block.BLUE_WOOL, 4, 100, 100)
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
