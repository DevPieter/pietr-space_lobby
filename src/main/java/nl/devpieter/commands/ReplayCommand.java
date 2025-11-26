package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.entity.Player;
import nl.devpieter.features.parkour.ParkourReplayManager;
import nl.devpieter.features.parkour.models.parkourReplay.FinishedParkourReplay;

import java.util.UUID;

public class ReplayCommand extends Command {

    private final ParkourReplayManager replayManager = ParkourReplayManager.getInstance();

    public ReplayCommand() {
        super("replay");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /replay <parkour>");
        });

        Command parkour = new Command("parkour");
        ArgumentUUID parkourIdArg = ArgumentType.UUID("parkourId");

        parkour.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            UUID parkourId = context.get(parkourIdArg);
            FinishedParkourReplay replay = replayManager.getFinishedReplay(parkourId);

            if (replay == null) {
                player.sendMessage("No replay found with the given ID.");
                return;
            }

            boolean started = replay.startReplay(player);
            if (!started) player.sendMessage("A replay is already in progress.");
        }, parkourIdArg);

        addSubcommand(parkour);
    }
}
