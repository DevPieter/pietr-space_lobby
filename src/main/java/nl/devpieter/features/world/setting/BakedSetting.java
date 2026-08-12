package nl.devpieter.features.world.setting;

import org.jetbrains.annotations.Nullable;

public class BakedSetting<T extends IClonable<T>> implements IFreezable {

    private boolean isFrozen = false;

    private T value;
    private @Nullable T frozenValue;

    public BakedSetting(T value) {
        this.value = value;
    }

    public T value() {
        if (isFrozen) return frozenValue;
        return value;
    }

    public T nonFrozenValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public void freeze() {
        IFreezable.super.freeze();
        frozenValue = value.cloneObj();
        isFrozen = true;
    }

    @Override
    public void melt() {
        IFreezable.super.melt();
        frozenValue = null;
        isFrozen = false;
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }
}
