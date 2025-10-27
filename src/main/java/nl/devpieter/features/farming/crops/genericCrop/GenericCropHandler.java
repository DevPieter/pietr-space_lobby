package nl.devpieter.features.farming.crops.genericCrop;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.features.farming.crops.genericCrop.enums.GenericCrop;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericCropHandler extends FarmBlockHandler {

    public GenericCropHandler(GenericCrop crop) {
        this(2_000, 4_000, crop.getMaxGrowthStage());
    }

    public GenericCropHandler(long minGrowInterval, long maxGrowInterval, int maxAge) {
        super(minGrowInterval, maxGrowInterval, maxAge);

        setReplantSound(Sound.sound(SoundEvent.ITEM_CROP_PLANT, Sound.Source.BLOCK, 0.45f, 0.96f));
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "generic_crop");
    }

    @Override
    protected int getExperienceOnHarvest(@NotNull BlockHolder holder, @Nullable Entity harvester, int age) {
        return 3;
    }
}
