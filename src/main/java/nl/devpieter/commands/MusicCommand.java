package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;
import nl.devpieter.features.jukebox.MusicManager;

public class MusicCommand extends Command {

    private final MusicManager musicManager = MusicManager.getInstance();

    public MusicCommand() {
        super("music");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /music {trackUrl}");
        });

        ArgumentString trackUrlArgument = new ArgumentString("trackUrl");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            String trackUrl = context.get(trackUrlArgument);

            if (!musicManager.isPlayerSubscribed(player.getUuid())) {
                player.sendMessage("You are not subscribed to a jukebox.");
                return;
            }

            if (musicManager.playerPreQueueSong(player.getUuid(), trackUrl)) {
                player.sendMessage("Queued song: " + trackUrl);
            } else {
                player.sendMessage("Failed to queue song: " + trackUrl);
            }
        }, trackUrlArgument);
    }
}