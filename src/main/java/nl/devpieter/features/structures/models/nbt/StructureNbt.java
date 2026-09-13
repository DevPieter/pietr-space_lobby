package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record StructureNbt(
        StructureSize size,
        List<StructurePalette> palette,
        List<StructureBlock> blocks,
        List<StructureEntity> entities
) {
    public static @Nullable StructureNbt fromNbt(CompoundBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        StructureSize size = StructureSize.fromNbt(nbt.getList("size"));
        if (size == null) return null;

        List<StructurePalette> palette = StructurePalette.fromNbtList(nbt.getList("palette"));
        List<StructureBlock> blocks = StructureBlock.fromNbtList(nbt.getList("blocks"));
        List<StructureEntity> entities = StructureEntity.fromNbtList(nbt.getList("entities"));

        return new StructureNbt(
                size,
                palette,
                blocks,
                entities
        );
    }

    private static boolean isValidNbt(CompoundBinaryTag nbt) {
        return nbt.contains("size") &&
                nbt.contains("palette") &&
                nbt.contains("blocks") &&
                nbt.contains("entities");
    }
}
