package nl.devpieter.features.jukebox.blockHandlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockHandler;
import nl.devpieter.features.jukebox.MusicManager;
import nl.devpieter.sees.listener.SListener;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JukeboxBlockHandler3 implements BlockHandler, SListener {

    //    private final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
//    private final MusicJobManager musicJobManager = MusicJobManager.getInstance();
//    private final List<UUID> subscribedPlayers = new ArrayList<>();

    private final UUID jukeboxId = UUID.fromString("9d9c4136-fd5f-4ef0-af9e-cdec6a1cde5d");
    private final MusicManager musicManager = MusicManager.getInstance();

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "jukebox");
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getHand() != PlayerHand.MAIN) return false;
        Player player = interaction.getPlayer();

//        List<String> songUrls = List.of(
////                "XM7BVj_C7PI" // Klaag zang
////                "fnQrPvm0_38" // Beseffen
////                "PtApgLa94Yo"
//                "dbIpxy59B-U"
//        );
//
//        musicManager.queueSong("dbIpxy59B-U");

        if (player.isSneaking()) {
            player.sendMessage(TextUtils.miniText("<yellow>Queuing songs..."));

            musicManager.queueSongs(jukeboxId).whenComplete((queued, throwable) -> {
                if (throwable != null) {
                    player.sendMessage(TextUtils.miniText("<red>Failed to queue songs: " + throwable.getMessage()));
                    return;
                }

                if (queued) {
                    player.sendMessage(TextUtils.miniText("<green>Songs have been queued successfully."));
                } else {
                    player.sendMessage(TextUtils.miniText("<yellow>No songs were queued."));
                }
            });
        } else {
            toggleSubscription(player);
        }
        return true;
    }

    private void toggleSubscription(@NotNull Player player) {
        UUID playerUuid = player.getUuid();

        if (musicManager.isPlayerSubscribed(playerUuid)) {
            musicManager.unsubscribe(playerUuid, jukeboxId);
            player.sendMessage(TextUtils.miniText("<red>You have unsubscribed from jukebox updates."));
        } else {
            musicManager.subscribe(playerUuid, jukeboxId);
            player.sendMessage(TextUtils.miniText("<green>You have subscribed to jukebox updates."));
        }
    }
}
