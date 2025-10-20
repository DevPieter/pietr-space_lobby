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
                case SCAN -> {
                    player.sendMessage("Scanning for farms...");
                    int foundFarms = farmManager.scanFarms();
                    player.sendMessage("Found " + foundFarms + " farms.");
                }
                case SHUFFLE -> {
                    farmManager.shuffleFarms();
                    player.sendMessage("Farms have been shuffled.");
                }
                case CLEAR -> {
                    farmManager.clearFarms();
                    player.sendMessage("Farms have been cleared.");
                }
            }
        }, actionArgument);
    }

    enum Action {
        SCAN,
        SHUFFLE,
        CLEAR
    }
}
