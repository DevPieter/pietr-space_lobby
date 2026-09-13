package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.Nullable;

public record StructureDoublePosition(
        double x,
        double y,
        double z
) {
    public static @Nullable StructureDoublePosition fromNbt(ListBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        return new StructureDoublePosition(
                nbt.getDouble(0),
                nbt.getDouble(1),
                nbt.getDouble(2)
        );
    }

    public static boolean isValidNbt(ListBinaryTag nbt) {
        return nbt.size() == 3;
    }
}
