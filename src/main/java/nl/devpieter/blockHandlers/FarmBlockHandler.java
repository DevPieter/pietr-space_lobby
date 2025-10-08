package nl.devpieter.blockHandlers;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;

import java.awt.*;

public abstract class FarmBlockHandler implements BlockHandler {

    protected final long minGrowInterval;
    protected final long maxGrowInterval;
    protected final int maxAge;

    private long lastGrowTime = System.currentTimeMillis();
    private long nextGrowInterval = -1;

    protected FarmBlockHandler(long minGrowInterval, long maxGrowInterval, int maxAge) {
        this.minGrowInterval = minGrowInterval;
        this.maxGrowInterval = maxGrowInterval;
        this.maxAge = maxAge;
    }

    @Override
    public void tick(Tick tick) {
        Block block = tick.getBlock();
        Instance instance = tick.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return;

        int age = Integer.parseInt(ageProperty);
        if (age >= maxAge) return;

        if (nextGrowInterval == -1) {
            nextGrowInterval = (int) (Math.random() * (maxGrowInterval - minGrowInterval)) + (int) minGrowInterval;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGrowTime < nextGrowInterval) return;

        lastGrowTime = currentTime;
        nextGrowInterval = -1;

        instance.setBlock(tick.getBlockPosition(), block.withProperty("age", String.valueOf(age + 1)));
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    public void replant(Instance instance, Block block, Point position) {
        instance.setBlock(position, block.withProperty("age", "0"));

        nextGrowInterval = -1;
        lastGrowTime = System.currentTimeMillis();
    }
}
