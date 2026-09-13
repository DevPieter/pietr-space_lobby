package nl.devpieter.features.structures.models.nbt;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record StructureBlock(
        int state,
        StructureIntPosition pos,
        @Nullable CompoundBinaryTag nbt
) {
    public static List<StructureBlock> fromNbtList(ListBinaryTag nbt) {
        List<StructureBlock> blocks = new ArrayList<>();

        for (int i = 0; i < nbt.size(); i++) {
            CompoundBinaryTag tag = (CompoundBinaryTag) nbt.get(i).asBinaryTag();

            StructureBlock structureBlock = StructureBlock.fromNbt(tag);
            if (structureBlock != null) blocks.add(structureBlock);
        }

        return blocks;
    }

    public static @Nullable StructureBlock fromNbt(CompoundBinaryTag nbt) {
        if (!isValidNbt(nbt)) return null;

        StructureIntPosition position = StructureIntPosition.fromNbt(nbt.getList("pos"));
        if (position == null) return null;

        return new StructureBlock(
                nbt.getInt("state"),
                position,
                nbt.getCompound("nbt")
        );
    }

    public static boolean isValidNbt(CompoundBinaryTag nbt) {
        return nbt.contains("state") && nbt.contains("pos");
    }
}
