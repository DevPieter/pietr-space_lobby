package nl.devpieter.features.farming.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class FarmBerryCropHandler extends FarmBlockHandler {

    public FarmBerryCropHandler() {
        super(5_000, 8_000, 3);

        setReplantSound(Sound.sound(Key.key("block.sweet_berry_bush.pick_berries"), Sound.Source.BLOCK, 0.8f, 0.8f));
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        Block block = interaction.getBlock();
        Instance instance = interaction.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return false;

        int age = Integer.parseInt(ageProperty);
        if (age < 3) return false;

        replant(instance, block, interaction.getBlockPosition(), interaction.getPlayer());
        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "farm_berry_crop");
    }
}
