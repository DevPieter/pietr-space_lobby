package nl.devpieter.commands.world;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import nl.devpieter.features.farming.FarmManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FarmCommand extends Command {

    private final FarmManager farmManager = FarmManager.getInstance();

    public FarmCommand() {
        super("farm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /farm <action>");
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
                    player.sendMessage("Initializing, this may take a while...");
                    farmManager.init();
                    player.sendMessage("Initialization complete.");
                }
                case SHUFFLE -> {
                    farmManager.shuffleFarms();
                    player.sendMessage("Farms shuffled.");
                }
                case REVERT -> {
                    farmManager.revertFarms();
                    player.sendMessage("Farms reverted.");
                }
                // TODO - Convert into subcommands
                case DEBUG_PLACE -> {
                    farmManager.placeDebugMarkers();
                    player.sendMessage("Farm debug markers placed.");
                }
                case DEBUG_CLEAR -> {
                    farmManager.clearDebugMarkers();
                    player.sendMessage("Farm debug markers cleared.");
                }
            }
        }, actionArgument);
    }

    enum Action {
        INIT,
        SHUFFLE,
        REVERT,
        DEBUG_PLACE,
        DEBUG_CLEAR
    }
}
