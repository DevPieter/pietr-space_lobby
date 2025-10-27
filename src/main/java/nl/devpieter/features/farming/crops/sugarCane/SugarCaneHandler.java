package nl.devpieter.features.farming.crops.sugarCane;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.utils.ParticleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SugarCaneHandler extends FarmBlockHandler {

    private final Point base;
    private final List<Point> offspring = new ArrayList<>();

    public SugarCaneHandler(Point base) {
        super(5_000, 8_000, 3);
        this.base = base;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "sugar_cane_crop");
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
        return !offspring.isEmpty();
    }

    @Override
    public int getAge(@NotNull BlockHolder holder) {
        return offspring.size() + 1;
    }

    @Override
    protected void grow(@NotNull BlockHolder holder, int currentAge, int newAge) {
        if (!canGrow(holder)) return;
        onGrow(holder);

        Pos newPos = base.asPos().add(0, currentAge, 0);
        holder.instance().setBlock(newPos, holder.block());
        offspring.add(newPos);
    }

    @Override
    public void replant(@NotNull BlockHolder holder, @Nullable Entity replanter) {
        onReplant(holder, replanter);

        Instance instance = holder.instance();
        Block block = holder.block();

        Pos basePos = base.asPos();
        int height = getAge(holder);

        for (int y = 0; y < height; y++) {
            Pos removePos = basePos.add(0, y, 0);
            instance.setBlock(removePos, Block.AIR);
            offspring.remove(removePos);
        }

        instance.setBlock(basePos, block);

        nextGrowInterval = -1;
        lastGrowTime = System.currentTimeMillis();
    }

    @Override
    protected void onReplant(@NotNull BlockHolder holder, @Nullable Entity replanter) {
        super.onReplant(holder, replanter);
        if (replanter == null) return;

        for (Point pos : offspring) {
            ParticleUtils.spawnParticleToViewersAndSelf(
                    replanter,
                    Particle.HAPPY_VILLAGER,
                    pos.add(0.5, 0.5, 0.5),
                    new Pos(0.25, 0.25, 0.25),
                    0f,
                    4
            );
        }
    }

    private boolean canGrow(BlockHolder holder) {
        Pos topPos = base.asPos().add(0, getAge(holder), 0);
        Block topBlock = holder.instance().getBlock(topPos);

        return topBlock.isAir();
    }
}
