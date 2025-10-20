package nl.devpieter.features.farming.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public class FarmCropHandler extends FarmBlockHandler {

    public FarmCropHandler(int maxAge) {
        this(2_000, 4_000, maxAge);
    }

    public FarmCropHandler(long minGrowInterval, long maxGrowInterval, int maxAge) {
        super(minGrowInterval, maxGrowInterval, maxAge);

        setReplantSound(Sound.sound(Key.key("item.crop.plant"), Sound.Source.BLOCK, 0.45f, 0.96f));
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "farm_crop");
    }
}
