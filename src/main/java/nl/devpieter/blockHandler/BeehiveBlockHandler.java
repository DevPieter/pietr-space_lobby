package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.Direction;
import nl.devpieter.entities.bee.BeeEntity;
import nl.devpieter.utils.minestom.MineMathUtils;
import nl.devpieter.utils.minestom.SoundUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BeehiveBlockHandler implements BlockHandler {

    private final List<Acquirable<@NotNull ? extends BeeEntity>> bees = new ArrayList<>();

    private Instance instance;
    private Point hivePosition;
    private Pos beeSpawnPosition;

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "beehive");
    }

    @Override
    public void onPlace(Placement placement) {
        String facingProperty = placement.getBlock().getProperty("facing");
        if (facingProperty == null) facingProperty = "north";

        Direction facing = MineMathUtils.getFacingDirection(facingProperty);

        instance = placement.getInstance();
        hivePosition = placement.getBlockPosition();

        beeSpawnPosition = hivePosition.add(facing.mul(1).add(0.5, 0.5, 0.5)).asPos();
        beeSpawnPosition = beeSpawnPosition.withYaw(MineMathUtils.getYawFromDirection(facing));

        for (int i = 0; i < 3; i++) {
            BeeEntity bee = new BeeEntity(beeSpawnPosition, this::enterHive);
            bee.setInstance(instance, beeSpawnPosition);
            bees.add((Acquirable<? extends BeeEntity>) bee.acquirable());
        }
    }

    @Override
    public void onDestroy(Destroy destroy) {
        for (Acquirable<? extends BeeEntity> beeAcquirable : bees) {
            beeAcquirable.sync(Entity::remove);
        }

        bees.clear();
    }

    private void enterHive(@NonNull Acquirable<@NotNull ? extends BeeEntity> bee) {
        SoundUtils.playSoundInWorld(
                instance,
                beeSpawnPosition,
                Sound.sound(
                        SoundEvent.BLOCK_BEEHIVE_ENTER,
                        Sound.Source.NEUTRAL,
                        1.0f,
                        1.0f
                )
        );

        bee.sync(beeEntity -> {
            beeEntity.resetNectar();

            beeEntity.setFrozen(true);
            beeEntity.setInvisible(true);
            beeEntity.teleport(hivePosition.asPos().add(0.5, 0.5, 0.5));
        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            exitHive(bee);
        }, TaskSchedule.seconds(5), TaskSchedule.stop());
    }

    private void exitHive(@NonNull Acquirable<@NotNull ? extends BeeEntity> bee) {
        SoundUtils.playSoundInWorld(
                instance,
                beeSpawnPosition,
                Sound.sound(
                        SoundEvent.BLOCK_BEEHIVE_EXIT,
                        Sound.Source.NEUTRAL,
                        1.0f,
                        1.0f
                )
        );

        bee.sync(beeEntity -> {
            beeEntity.teleport(beeSpawnPosition);
            beeEntity.setFrozen(false);
            beeEntity.setInvisible(false);
        });
    }
}
