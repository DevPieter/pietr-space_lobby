package nl.devpieter.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class FarmWheatCropHandler implements BlockHandler {

    private final long minGrowInterval = 2_000;
    private final long maxGrowInterval = 6_000;

    private long lastGrowTime = System.currentTimeMillis();
    private long nextGrowInterval = -1;

    @Override
    public void tick(Tick tick) {
        Block block = tick.getBlock();
        Instance instance = tick.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return;

        int age = Integer.parseInt(ageProperty);
        if (age >= 7) return;

        if (nextGrowInterval == -1) {
            nextGrowInterval = minGrowInterval + (long) (Math.random() * (maxGrowInterval - minGrowInterval));
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGrowTime >= nextGrowInterval) {
            age++;

            instance.setBlock(tick.getBlockPosition(), block.withProperty("age", String.valueOf(age)));

            lastGrowTime = currentTime;
            nextGrowInterval = -1;
        }
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "farm_wheat_crop");
    }
}
