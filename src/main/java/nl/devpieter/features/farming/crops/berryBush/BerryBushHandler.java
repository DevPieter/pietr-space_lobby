package nl.devpieter.features.farming.crops.berryBush;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BerryBushHandler extends FarmBlockHandler {

    public BerryBushHandler() {
        super(5_000, 8_000, 3);

        setReplantSound(Sound.sound(SoundEvent.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, Sound.Source.BLOCK, 0.8f, 0.8f));
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "berry_bush_crop");
    }

    @Override
    protected int getExperienceOnHarvest(@NotNull BlockHolder holder, @Nullable Entity harvester, int age) {
        return switch (age) {
            case 2 -> 2;
            case 3 -> 4;
            default -> 0;
        };
    }

    @Override
    public boolean canBeHarvested(@NotNull BlockHolder holder) {
        return getAge(holder) >= 2;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        Instance instance = interaction.getInstance();
        Block block = interaction.getBlock();

        BlockHolder holder = new BlockHolder(instance, interaction.getBlockPosition(), block);
        if (!canBeHarvested(holder)) return false;

        replant(holder, interaction.getPlayer());
        return true;
    }
}
