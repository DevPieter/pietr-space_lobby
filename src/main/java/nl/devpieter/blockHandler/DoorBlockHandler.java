package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.pietr.lobby.common.utils.common.RandomUtils;
import space.pietr.lobby.common.utils.minestom.SoundUtils;

import java.time.Duration;

public class DoorBlockHandler implements BlockHandler {

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();
    private Task closeTask;

    @Override
    public boolean onInteract(Interaction interaction) {
        boolean isOpen = "true".equals(interaction.getBlock().getProperty("open"));
        boolean isHingeLeft = "left".equals(interaction.getBlock().getProperty("hinge"));
        boolean newState = !isOpen;

        Instance instance = interaction.getInstance();
        Block block = interaction.getBlock();
        Point point = interaction.getBlockPosition();
        Player player = interaction.getPlayer();

        setDoorState(instance, block, point, player, newState);
        cancelCloseTask();

        if (newState) closeTask = scheduler.buildTask(() -> {
            setDoorState(instance, block, point, null, false);

            SoundUtils.playSoundInWorld(
                    instance,
                    getSoundPosition(block, point),
                    SoundEvent.ENTITY_PLAYER_BREATH,
                    0.7f,
                    RandomUtils.randomFloatInclusive(0.6f, 1.0f, 0.01f)
            );
        }).delay(Duration.ofSeconds(RandomUtils.randomIntInclusive(6, 12))).schedule();

        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "door_block");
    }

    private void cancelCloseTask() {
        if (closeTask == null) return;
        if (!closeTask.isAlive()) return;

        closeTask.cancel();
        closeTask = null;
    }

    private void setDoorState(Instance instance, Block block, Point point, @Nullable Player player, boolean open) {
        String newState = open ? "true" : "false";

        boolean isUpperPart = "upper".equals(block.getProperty("half"));
        Point lowerPos = isUpperPart ? point.withY(point.y() - 1) : point;
        Point upperPos = isUpperPart ? point : point.withY(point.y() + 1);

        Block lower = instance.getBlock(lowerPos);
        Block upper = instance.getBlock(upperPos);

        Block newLower = lower.withProperty("open", newState);
        Block newUpper = upper.withProperty("open", newState);

        instance.setBlock(lowerPos, newLower);
        instance.setBlock(upperPos, newUpper);

        Sound sound = Sound.sound(
                open ? SoundEvent.BLOCK_WOODEN_DOOR_OPEN : SoundEvent.BLOCK_WOODEN_DOOR_CLOSE,
                Sound.Source.BLOCK,
                0.9f,
                1f
        );

        Point soundPos = getSoundPosition(newLower, lowerPos);
        if (player != null) SoundUtils.playSoundToViewers(player, soundPos, sound);
        else SoundUtils.playSoundInWorld(instance, soundPos, sound);
    }

    private Point getSoundPosition(Block block, Point point) {
        String facing = block.getProperty("facing");
        if (facing == null) return point.add(0.5, 0.5, 0.5);

        String hinge = block.getProperty("hinge");
        if (hinge == null) return point.add(0.5, 0.5, 0.5);

        Point offset = switch (facing) {
            case "north" -> hinge.equals("left") ? new Vec(-0.3, 0, 0) : new Vec(0.3, 0, 0);
            case "south" -> hinge.equals("left") ? new Vec(0.3, 0, 0) : new Vec(-0.3, 0, 0);
            case "west" -> hinge.equals("left") ? new Vec(0, 0, 0.3) : new Vec(0, 0, -0.3);
            case "east" -> hinge.equals("left") ? new Vec(0, 0, -0.3) : new Vec(0, 0, 0.3);
            default -> new Vec(0, 0, 0);
        };

        return point.add(0.5 + offset.x(), 0.5 + offset.y(), 0.5 + offset.z());
    }
}
