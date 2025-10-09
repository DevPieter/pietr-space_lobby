package nl.devpieter.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FarmBlockHandler implements BlockHandler {

    protected final long minGrowInterval;
    protected final long maxGrowInterval;
    protected final int maxAge;

    private Sound replantSound = null;
    private Sound growSound = Sound.sound(Key.key("item.bone_meal.use"), Sound.Source.BLOCK, 0.3f, 1.0f);

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
        onGrow(instance, block, tick.getBlockPosition());
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    public void replant(@NotNull Instance instance, @NotNull Block block, Point position, @Nullable Entity replanter) {
        instance.setBlock(position, block.withProperty("age", "0"));
        onReplant(instance, block, position, replanter);

        nextGrowInterval = -1;
        lastGrowTime = System.currentTimeMillis();
    }

    protected void onGrow(Instance instance, Block block, Point position) {
        playSound(instance, position, growSound);
    }

    protected void onReplant(Instance instance, Block block, Point position, @Nullable Entity replanter) {
        playSound(instance, position, replantSound);
        if (replanter == null) return;

        replanter.sendPacketToViewersAndSelf(new ParticlePacket(
                Particle.HAPPY_VILLAGER,
                position.add(0.5, 0.5, 0.5),
                new Pos(0.25, 0.25, 0.25),
                0,
                4
        ));
    }

    protected void setReplantSound(Sound sound) {
        this.replantSound = sound;
    }

    protected void setGrowSound(Sound sound) {
        this.growSound = sound;
    }

    protected void playSound(Instance instance, Point position, Sound sound) {
        if (sound != null) instance.playSound(sound, position);
    }
}
