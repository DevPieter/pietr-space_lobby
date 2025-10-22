package nl.devpieter.features.farming.crops.genericCrop;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.features.farming.crops.genericCrop.enums.GenericCrop;
import org.jetbrains.annotations.NotNull;

public class GenericCropHandler extends FarmBlockHandler {

    public GenericCropHandler(GenericCrop crop) {
        this(2_000, 4_000, crop.getMaxGrowthStage());
    }

    public GenericCropHandler(long minGrowInterval, long maxGrowInterval, int maxAge) {
        super(minGrowInterval, maxGrowInterval, maxAge);

        setReplantSound(Sound.sound(Key.key("item.crop.plant"), Sound.Source.BLOCK, 0.45f, 0.96f));
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "generic_crop");
    }
}
