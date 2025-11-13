package nl.devpieter.features.mining.miningSequence;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.models.MiningStep;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMiningSequence {

    int getRevertTicks();

    List<MiningStep> getSteps();

    boolean isAtBeginning();

    boolean isAtEnd();

    @Nullable MiningStep increment();

    @Nullable MiningStep decrement();

    @Nullable MiningStep getPreviousStep();

    MiningStep getCurrentStep();

    Block buildBlockFromCurrentStep();
}
