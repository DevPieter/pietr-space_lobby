package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import nl.devpieter.utils.MathUtils;
import nl.devpieter.utils.SoundUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class DoorBlockHandler implements BlockHandler {

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();
    private Task closeTask;

    @Override
    public boolean onInteract(Interaction interaction) {
        boolean isOpen = "true".equals(interaction.getBlock().getProperty("open"));
        boolean newState = !isOpen;

        setDoorState(interaction.getInstance(), interaction.getBlockPosition(), interaction.getPlayer(), newState);
        cancelCloseTask();

        if (newState) closeTask = scheduler.buildTask(() -> {
            setDoorState(interaction.getInstance(), interaction.getBlockPosition(), null, false);

            Sound sound = Sound.sound(
                    Key.key("minecraft", "entity.player.breath"),
                    Sound.Source.AMBIENT,
                    0.7f,
                    MathUtils.randomFloat(0.6f, 1.0f)
            );

            SoundUtils.playSoundInWorld(interaction.getInstance(), interaction.getBlockPosition().add(0.5, 0.5, 0.5), sound);
        }).delay(Duration.ofSeconds(8)).schedule();

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

    private void setDoorState(Instance instance, Point point, @Nullable Player player, boolean open) {
        Block block = instance.getBlock(point);
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
                open ? Key.key("minecraft", "block.wooden_door.open") : Key.key("minecraft", "block.wooden_door.close"),
                Sound.Source.BLOCK,
                0.9f,
                1f
        );

        if (player != null) SoundUtils.playSoundToViewers(player, lowerPos.add(0.5, 0.5, 0.5), sound);
        else SoundUtils.playSoundInWorld(instance, lowerPos.add(0.5, 0.5, 0.5), sound);
    }
}
