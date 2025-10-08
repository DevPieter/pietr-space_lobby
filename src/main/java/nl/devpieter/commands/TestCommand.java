package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import nl.devpieter.managers.FarmManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /test");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        addSyntax((sender, context) -> {
            FarmManager.getInstance().init();
        });
    }
}
