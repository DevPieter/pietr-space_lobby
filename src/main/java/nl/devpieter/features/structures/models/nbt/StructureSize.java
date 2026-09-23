package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.collision.BoundingBox;
import org.jetbrains.annotations.Nullable;

public record StructureSize(
        int width,
        int height,
        int depth
) {
    public BoundingBox asBoundingBox() {
        return new BoundingBox(width, height, depth);
    }

    public static @Nullable StructureSize fromNbt(ListBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        return new StructureSize(
                nbt.getInt(0),
                nbt.getInt(1),
                nbt.getInt(2)
        );
    }

    public static boolean isValidNbt(ListBinaryTag nbt) {
        return nbt.size() == 3;
    }
}