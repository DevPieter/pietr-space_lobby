package nl.devpieter;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public class BerryBlockHandler implements BlockHandler {

    @Override
    public boolean onInteract(Interaction interaction) {
        Block block = interaction.getBlock();
        Instance instance = interaction.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return false;

        int age = Integer.parseInt(ageProperty);
        if (age < 3) return false;

        instance.setBlock(interaction.getBlockPosition(), block.withProperty("age", "1"));

        Sound sound = Sound.sound(Key.key("block.sweet_berry_bush.pick_berries"), Sound.Source.BLOCK, 0.8f, 0.8f);
        instance.playSound(sound, interaction.getBlockPosition());

        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "sweet_berry_bush");
    }
}
