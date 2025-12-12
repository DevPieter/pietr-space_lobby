package nl.devpieter.commands.player;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /gamemode <survival|creative|adventure|spectator>");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        ArgumentEnum<@NotNull GameMode> modeArgument = ArgumentType.Enum("mode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            GameMode mode = context.get(modeArgument);
            if (!(sender instanceof Player player)) return;

            boolean fromSpectator = player.getGameMode() == GameMode.SPECTATOR;
            boolean toSpectator = mode == GameMode.SPECTATOR;

            player.setGameMode(mode);

            if (toSpectator) {
                player.setInvisible(true);
                player.setAutoViewable(false);
            } else if (fromSpectator) {
                player.setInvisible(false);
                player.setAutoViewable(true);
            }

            player.sendMessage("Your game mode has been set to " + mode.name());
        }, modeArgument);
    }
}
