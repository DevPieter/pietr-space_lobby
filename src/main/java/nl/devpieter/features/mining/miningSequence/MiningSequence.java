package nl.devpieter.features.mining.miningSequence;

import net.minestom.server.instance.block.Block;
import nl.devpieter.features.mining.models.MiningStep;
import org.jetbrains.annotations.Nullable;

public abstract class MiningSequence implements IMiningSequence {

    private int currentIndex;

    @Override
    public boolean isAtBeginning() {
        return getSteps().isEmpty() || currentIndex == 0;
    }

    @Override
    public boolean isAtEnd() {
        return getSteps().isEmpty() || currentIndex >= getSteps().size() - 1;
    }

    @Override
    public @Nullable MiningStep increment() {
        if (isAtEnd()) return null;

        currentIndex++;
        return getSteps().get(currentIndex);
    }

    @Override
    public @Nullable MiningStep decrement() {
        if (isAtBeginning()) return null;

        currentIndex--;
        return getSteps().get(currentIndex);
    }

    @Override
    public @Nullable MiningStep getPreviousStep() {
        if (isAtBeginning()) return null;
        return getSteps().get(currentIndex - 1);
    }

    @Override
    public MiningStep getCurrentStep() {
        return getSteps().get(currentIndex);
    }

    @Override
    public Block buildBlockFromCurrentStep() {
        MiningStep currentStep = getCurrentStep();
        return currentStep.block().withHandler(currentStep.blockHandler());
    }
}
