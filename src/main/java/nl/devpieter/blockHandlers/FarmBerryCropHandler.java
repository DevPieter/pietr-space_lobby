package nl.devpieter.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class FarmBerryCropHandler implements BlockHandler {

    private final long minGrowInterval = 5_000;
    private final long maxGrowInterval = 12_000;

    private long lastGrowTime = System.currentTimeMillis();
    private long nextGrowInterval = -1;

    @Override
    public void tick(Tick tick) {
        Block block = tick.getBlock();
        Instance instance = tick.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return;

        int age = Integer.parseInt(ageProperty);
        if (age >= 3) return;

        if (nextGrowInterval == -1) {
            nextGrowInterval = minGrowInterval + (long) (Math.random() * (maxGrowInterval - minGrowInterval));
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGrowTime >= nextGrowInterval) {
            age++;

            Block newBlock = Block.SWEET_BERRY_BUSH.withHandler(this).withProperty("age", String.valueOf(age));
            instance.setBlock(tick.getBlockPosition(), newBlock);

            lastGrowTime = currentTime;
            nextGrowInterval = -1;
        }
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        Block block = interaction.getBlock();
        Instance instance = interaction.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return false;

        int age = Integer.parseInt(ageProperty);
        if (age < 3) return false;

        instance.setBlock(interaction.getBlockPosition(), block.withProperty("age", "0"));

        Sound sound = Sound.sound(Key.key("block.sweet_berry_bush.pick_berries"), Sound.Source.BLOCK, 0.8f, 0.8f);
        instance.playSound(sound, interaction.getBlockPosition());

        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "farm_berry_crop");
    }
}
