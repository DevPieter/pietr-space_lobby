package nl.devpieter.blockHandlers;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class FarmCropHandler extends FarmBlockHandler {

    public FarmCropHandler() {
        super(2_000, 6_000, 7);
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "farm_crop");
    }
}
