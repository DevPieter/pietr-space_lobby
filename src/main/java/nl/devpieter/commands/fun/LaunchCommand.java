package nl.devpieter.commands.fun;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LaunchCommand extends Command {

    public LaunchCommand() {
        super("launch");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /launch <player> <direction> <strength>");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        ArgumentEntity playerArgument = ArgumentType.Entity("player").onlyPlayers(true);
        ArgumentEnum<@NotNull Direction> directionArgument = ArgumentType.Enum("direction", Direction.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        ArgumentDouble strengthArgument = ArgumentType.Double("strength");

        addSyntax((sender, context) -> {
            Player target = context.get(playerArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("Player not found");
                return;
            }

            final Direction direction = context.get(directionArgument);
            final double strength = context.get(strengthArgument);

            double x = 0;
            double y = 8;
            double z = 0;

            switch (direction) {
                case UP -> y = strength;
                case DOWN -> y = -strength;
                case NORTH -> z = -strength;
                case EAST -> x = strength;
                case SOUTH -> z = strength;
                case WEST -> x = -strength;
            }

            Vec velocity = new Vec(x, y, z);
            target.setVelocity(velocity);

            sender.sendMessage("Launched " + target.getUsername() + " " + direction.name().toLowerCase() + " with strength " + strength);
        }, playerArgument, directionArgument, strengthArgument);
    }

    private enum Direction {
        UP,
        DOWN,
        NORTH,
        EAST,
        SOUTH,
        WEST
    }
}
