package nl.devpieter.features.mining.miningSequence.sequences;

import nl.devpieter.features.mining.MiningSteps;
import nl.devpieter.features.mining.miningSequence.MiningSequence;
import nl.devpieter.features.mining.models.MiningStep;

import java.util.List;

public class CobblestoneMiningSequence extends MiningSequence {

    @Override
    public int getRevertTicks() {
        return 6 * 20;
    }

    @Override
    public List<MiningStep> getSteps() {
        return List.of(
                MiningSteps.COBBLESTONE,
                MiningSteps.FINAL_STEP
        );
    }
}
