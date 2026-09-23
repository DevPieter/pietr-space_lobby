package nl.devpieter.models;

import nl.devpieter.features.world.setting.IClonable;

public record SimpleRange(
        double min,
        double max
) implements IClonable<SimpleRange> {

    @Override
    public SimpleRange cloneObj() {
        return new SimpleRange(min, max);
    }
}
