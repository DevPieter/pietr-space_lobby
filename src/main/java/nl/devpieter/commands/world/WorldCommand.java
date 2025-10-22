package nl.devpieter.commands.world;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import nl.devpieter.features.world.WorldManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldCommand extends Command {

    private final WorldManager worldManager = WorldManager.getInstance();

    public WorldCommand() {
        super("world");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /world <action>");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        ArgumentEnum<@NotNull Action> actionArgument = ArgumentType.Enum("action", Action.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            Action action = context.get(actionArgument);
            if (!(sender instanceof Player player)) return;

            switch (action) {
                case INIT -> {
                    player.sendMessage("Initializing worlds...");
                    worldManager.init();
                    player.sendMessage("Worlds initialized.");
                }
            }
        }, actionArgument);
    }

    enum Action {
        INIT
    }
}
