package nl.devpieter.features.farming;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.particle.Particle;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.utils.ParticleUtils;
import nl.devpieter.utils.SoundUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FarmBlockHandler implements BlockHandler {

    protected final long minGrowInterval;
    protected final long maxGrowInterval;
    protected final int maxAge;

    protected long lastGrowTime = System.currentTimeMillis();
    protected long nextGrowInterval = -1;

    protected Sound replantSound = null;
    protected Sound growSound = Sound.sound(Key.key("item.bone_meal.use"), Sound.Source.BLOCK, 0.3f, 1.0f);

    protected FarmBlockHandler(long minGrowInterval, long maxGrowInterval, int maxAge) {
        this.minGrowInterval = minGrowInterval;
        this.maxGrowInterval = maxGrowInterval;
        this.maxAge = maxAge;
    }

    @Override
    public void tick(Tick tick) {
        BlockHolder holder = new BlockHolder(tick.getInstance(), tick.getBlockPosition(), tick.getBlock());

        int age = getAge(holder);
        if (age == -1 || age >= maxAge) return;

        if (nextGrowInterval == -1) {
            nextGrowInterval = (int) (Math.random() * (maxGrowInterval - minGrowInterval)) + (int) minGrowInterval;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGrowTime < nextGrowInterval) return;

        lastGrowTime = currentTime;
        nextGrowInterval = -1;

        grow(holder, age, age + 1);
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    public boolean isMature(@NotNull BlockHolder holder) {
        int age = getAge(holder);
        return age != -1 && age >= maxAge;
    }

    public boolean canBeHarvested(@NotNull BlockHolder holder) {
        return isMature(holder);
    }

    public int getAge(@NotNull BlockHolder holder) {
        Block block = holder.block();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return -1;

        return Integer.parseInt(ageProperty);
    }

    public int maxAge() {
        return maxAge;
    }

    protected void grow(@NotNull BlockHolder holder, int currentAge, int newAge) {
        Instance instance = holder.instance();
        Block block = holder.block();

        instance.setBlock(holder.position(), block.withProperty("age", String.valueOf(newAge)));
        onGrow(instance, block, holder.position());
    }

    public void replant(@NotNull BlockHolder holder, @Nullable Entity replanter) {
        Instance instance = holder.instance();
        Block block = holder.block();
        Point position = holder.position();

        instance.setBlock(position, block.withProperty("age", "0"));
        onReplant(instance, block, position, replanter);

        nextGrowInterval = -1;
        lastGrowTime = System.currentTimeMillis();
    }

    protected void onGrow(Instance instance, Block block, Point position) {
        SoundUtils.playSoundInWorld(instance, position, growSound);
    }

    protected void onReplant(Instance instance, Block block, Point position, @Nullable Entity replanter) {
        SoundUtils.playSoundInWorld(instance, position, growSound);
        if (replanter == null) return;

        ParticleUtils.spawnParticleToViewersAndSelf(
                replanter,
                Particle.HAPPY_VILLAGER,
                position.add(0.5, 0.5, 0.5),
                new Pos(0.25, 0.25, 0.25),
                0f,
                4
        );
    }

    protected void setReplantSound(Sound sound) {
        this.replantSound = sound;
    }

    protected void setGrowSound(Sound sound) {
        this.growSound = sound;
    }
}
