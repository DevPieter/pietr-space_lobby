package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class HangingSignBlockHandler implements BlockHandler {

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "hanging_sign_block");
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.NBT("front_text"),
                Tag.NBT("back_text")
        );
    }
}
