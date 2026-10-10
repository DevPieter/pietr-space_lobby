package space.pietr.lobby.common.models.simple;

import net.minestom.server.coordinate.BlockVec;
import org.jspecify.annotations.NonNull;
import space.pietr.lobby.common.types.IClonable;

public record SimpleBlockPos(
        int x,
        int y,
        int z
) implements IClonable<SimpleBlockPos> {

    public static final SimpleBlockPos ZERO = new SimpleBlockPos(0, 0, 0);

    public static @NonNull SimpleBlockPos of(int x, int y, int z) {
        return new SimpleBlockPos(x, y, z);
    }

    public @NonNull BlockVec toVec() {
        return new BlockVec(x, y, z);
    }

    public @NonNull SimpleVec toSimpleVec() {
        return new SimpleVec(x, y, z);
    }

    @Override
    public @NonNull SimpleBlockPos cloneObj() {
        return new SimpleBlockPos(x, y, z);
    }
}
