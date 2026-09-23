package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.Nullable;

public record StructureIntPosition(
        int x,
        int y,
        int z
) {
    public static @Nullable StructureIntPosition fromNbt(ListBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        return new StructureIntPosition(
                nbt.getInt(0),
                nbt.getInt(1),
                nbt.getInt(2)
        );
    }

    public static boolean isValidNbt(ListBinaryTag nbt) {
        return nbt.size() == 3;
    }
}
