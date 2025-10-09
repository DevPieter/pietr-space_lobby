package nl.devpieter.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public class FarmCropHandler extends FarmBlockHandler {

    public FarmCropHandler() {
        super(2_000, 4_000, 7);

        setReplantSound(Sound.sound(Key.key("item.crop.plant"), Sound.Source.BLOCK, 0.45f, 0.96f));
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "farm_crop");
    }
}
