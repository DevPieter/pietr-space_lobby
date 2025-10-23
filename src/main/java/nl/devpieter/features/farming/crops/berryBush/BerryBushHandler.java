package nl.devpieter.features.farming.crops.berryBush;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;

public class BerryBushHandler extends FarmBlockHandler {

    public BerryBushHandler() {
        super(5_000, 8_000, 3);

        setReplantSound(Sound.sound(Key.key("block.sweet_berry_bush.pick_berries"), Sound.Source.BLOCK, 0.8f, 0.8f));
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

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "berry_bush_crop");
    }
}
