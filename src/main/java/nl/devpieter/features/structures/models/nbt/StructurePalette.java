package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record StructurePalette(
        String name,
        @Nullable CompoundBinaryTag properties
) {
    public static List<StructurePalette> fromNbtList(ListBinaryTag nbt) {
        List<StructurePalette> palette = new ArrayList<>();

        for (int i = 0; i < nbt.size(); i++) {
            CompoundBinaryTag tag = (CompoundBinaryTag) nbt.get(i).asBinaryTag();

            StructurePalette structurePalette = StructurePalette.fromNbt(tag);
            if (structurePalette != null) palette.add(structurePalette);
        }

        return palette;
    }

    public static @Nullable StructurePalette fromNbt(CompoundBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        return new StructurePalette(
                nbt.getString("Name"),
                nbt.getCompound("Properties")
        );
    }

    public static boolean isValidNbt(CompoundBinaryTag nbt) {
        return nbt.contains("Name");
    }
}
