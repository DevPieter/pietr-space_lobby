package space.pietr.lobby.common.models.simple;

import space.pietr.lobby.common.types.IClonable;

public record SimpleRange(
        double min,
        double max
) implements IClonable<SimpleRange> {

    public static SimpleRange of(double min, double max) {
        return new SimpleRange(min, max);
    }

    @Override
    public SimpleRange cloneObj() {
        return new SimpleRange(min, max);
    }
}
