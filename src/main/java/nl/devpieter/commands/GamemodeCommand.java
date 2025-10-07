package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /gamemode <mode>");
        });

        ArgumentEnum<@NotNull GameMode> modeArgument = ArgumentType.Enum("mode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            final GameMode mode = context.get(modeArgument);
            if (!(sender instanceof Player player)) return;

            player.setGameMode(mode);
            player.sendMessage("Your game mode has been set to " + mode.name());
        }, modeArgument);
    }
}
