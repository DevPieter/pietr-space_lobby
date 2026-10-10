package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import nl.devpieter.features.parkour.ParkourReplayManager;
import nl.devpieter.features.parkour.models.replay.FinishedPlayerReplay;
import nl.devpieter.features.tips.TipManager;
import space.pietr.lobby.common.player.LobbyPlayer;

import java.util.UUID;

public class ReplayCommand extends Command {

    private final ParkourReplayManager replayManager = ParkourReplayManager.getInstance();
    private final TipManager tipManager = TipManager.getInstance();

    public ReplayCommand() {
        super("replay");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /replay <parkour>");
        });

        Command parkour = new Command("parkour");
        ArgumentUUID parkourIdArg = ArgumentType.UUID("parkourId");

        parkour.addSyntax((sender, context) -> {
            if (!(sender instanceof LobbyPlayer player)) return;

            UUID parkourId = context.get(parkourIdArg);
            FinishedPlayerReplay replay = replayManager.getFinishedReplay(parkourId);

            if (replay == null) {
                player.sendMessage("No replay found with the given ID.");
                return;
            }

            replayManager.playReplay(player, replay);

            tipManager.showTip(
                    player,
                    "replay_experimental_feature",
                    "Replay",
                    "Replays are an <b>experimental feature</b> and may not work as intended."
            );
        }, parkourIdArg);

        addSubcommand(parkour);
    }
}
