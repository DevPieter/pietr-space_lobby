package nl.devpieter.features.signs;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.signs.enums.SignMaterial;
import nl.devpieter.features.signs.enums.SignType;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.features.signs.models.SignHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class SignHelper {

    public static @Nullable SignHolder readSign(@NotNull BlockHolder blockHolder) {
        Block block = blockHolder.block();
        if (!block.hasNbt()) return null;

        SignType type = getSignType(block);
        if (type == SignType.UNKNOWN) return null;

        SignMaterial material = getSignMaterial(block);
        if (material == SignMaterial.UNKNOWN) return null;

        CompoundBinaryTag nbt = block.nbt();
        if (nbt == null) return null;

        List<String> lines;

        if (type == SignType.NORMAL || type == SignType.HANGING || type == SignType.WALL_HANGING) {
            lines = new ArrayList<>();
            lines.addAll(readFrontLines(nbt));
            lines.addAll(readBackLines(nbt));
        } else {
            lines = readFrontLines(nbt);
        }

        return new SignHolder(blockHolder, type, material, lines);
    }

    public static SignType getSignType(@NotNull Block block) {
        String name = block.name();
        if (!name.endsWith("sign")) return SignType.UNKNOWN;

        if (block.name().contains("wall_hanging")) return SignType.WALL_HANGING;
        else if (block.name().contains("hanging")) return SignType.HANGING;
        else if (block.name().contains("wall")) return SignType.WALL;
        else return SignType.NORMAL;
    }

    public static SignMaterial getSignMaterial(@NotNull Block block) {
        String name = block.name();
        if (!name.endsWith("sign")) return SignMaterial.UNKNOWN;

        for (SignMaterial material : SignMaterial.values()) {
            if (material == SignMaterial.UNKNOWN) continue;
            if (name.contains(material.name().toLowerCase() + "_")) return material;
        }

        return SignMaterial.UNKNOWN;
    }

    private static @NotNull @Unmodifiable List<String> readFrontLines(@NotNull CompoundBinaryTag nbt) {
        CompoundBinaryTag frontText = nbt.getCompound("front_text");
        return readLines(frontText.getList("messages", BinaryTagTypes.STRING));
    }

    private static @NotNull @Unmodifiable List<String> readBackLines(@NotNull CompoundBinaryTag nbt) {
        CompoundBinaryTag backText = nbt.getCompound("back_text");
        return readLines(backText.getList("messages", BinaryTagTypes.STRING));
    }

    private static @NotNull @Unmodifiable List<String> readLines(@NotNull ListBinaryTag messages) {
        return messages.stream().map(tag -> ((StringBinaryTag) tag).value()).toList();
    }
}
