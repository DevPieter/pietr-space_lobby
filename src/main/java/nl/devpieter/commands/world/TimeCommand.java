package nl.devpieter.commands.world;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class TimeCommand extends Command {

    public TimeCommand() {
        super("time");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time <time>");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        ArgumentInteger timeArgument = ArgumentType.Integer("time");

        addSyntax((sender, context) -> {
            int time = context.get(timeArgument);
            if (!(sender instanceof Player player)) return;

            player.getInstance().setTime(time);
            player.sendMessage("The time has been set to " + time);
        }, timeArgument);
    }
}
