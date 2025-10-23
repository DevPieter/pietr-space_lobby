package nl.devpieter.features.farming.crops.sugarCane;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SugarCaneHandler extends FarmBlockHandler {

    private final Point base;
    private final List<Point> offspring = new ArrayList<>();

    public SugarCaneHandler(Point base) {
        super(4_000, 6_000, 4);
        this.base = base;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "sugar_cane_crop");
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

        Instance instance = holder.instance();
        Block block = holder.block();

        Pos newPos = base.asPos().add(0, currentAge, 0);
        instance.setBlock(newPos, block);
        offspring.add(newPos);

        onGrow(instance, block, holder.position());
    }

    @Override
    public void replant(@NotNull BlockHolder holder, @Nullable Entity replanter) {
        Instance instance = holder.instance();
        Block block = holder.block();
        Pos position = holder.position().asPos();

        Pos basePos = base.asPos();
        int height = getAge(holder);

        onReplant(instance, block, position, replanter);

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
    protected void onReplant(Instance instance, Block block, Point position, @Nullable Entity replanter) {
        playSound(instance, position, replantSound);
        if (replanter == null) return;

        for (Point pos : offspring) {
            replanter.sendPacketToViewersAndSelf(new ParticlePacket(
                    Particle.HAPPY_VILLAGER,
                    pos.add(0.5, 0.5, 0.5),
                    new Pos(0.25, 0.25, 0.25),
                    0,
                    4
            ));
        }
    }

    private boolean canGrow(BlockHolder holder) {
        Pos topPos = base.asPos().add(0, getAge(holder), 0);
        Block topBlock = holder.instance().getBlock(topPos);

        return topBlock.isAir();
    }
}
