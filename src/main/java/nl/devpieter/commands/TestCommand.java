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


        addSyntax((sender, context) -> {
            FarmManager.getInstance().init();
        });
    }
}
