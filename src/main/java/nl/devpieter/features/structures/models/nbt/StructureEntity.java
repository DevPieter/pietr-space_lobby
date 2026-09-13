package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record StructureEntity(
        StructureDoublePosition pos,
        StructureIntPosition blockPos,
        @Nullable CompoundBinaryTag nbt
) {
    public static List<StructureEntity> fromNbtList(ListBinaryTag nbt) {
        List<StructureEntity> entities = new ArrayList<>();

        for (int i = 0; i < nbt.size(); i++) {
            CompoundBinaryTag tag = (CompoundBinaryTag) nbt.get(i).asBinaryTag();

            StructureEntity structureEntity = StructureEntity.fromNbt(tag);
            if (structureEntity != null) entities.add(structureEntity);
        }

        return entities;
    }

    public static @Nullable StructureEntity fromNbt(CompoundBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        StructureDoublePosition position = StructureDoublePosition.fromNbt(nbt.getList("pos"));
        if (position == null) return null;

        StructureIntPosition blockPosition = StructureIntPosition.fromNbt(nbt.getList("blockPos"));
        if (blockPosition == null) return null;

        return new StructureEntity(
                position,
                blockPosition,
                nbt.getCompound("nbt")
        );
    }

    public static boolean isValidNbt(CompoundBinaryTag nbt) {
        return nbt.contains("pos") && nbt.contains("blockPos");
    }
}
