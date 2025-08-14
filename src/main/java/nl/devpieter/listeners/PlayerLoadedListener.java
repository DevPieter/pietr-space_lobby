package nl.devpieter.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoadedEvent;
import nl.devpieter.managers.PlayerAdvancementManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLoadedListener implements EventListener<@NotNull PlayerLoadedEvent> {

//    private final HashMap<UUID, PlayerAdvancementManager> playerAdvancementManagers = new HashMap<>();

    // 🚀
    private final Component welcome = MiniMessage.miniMessage().deserialize(
            "<gray>Welcome to <b><gradient:#6A75AF:#303449>Pietr 🚀 Space</gradient></b>!\n" +
                    "<gray>Use <yellow>/server</yellow> to switch between worlds."
    );

    @Override
    public @NotNull Class<PlayerLoadedEvent> eventType() {
        return PlayerLoadedEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerLoadedEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(this.welcome);

//        this.advancementService.addViewer(player);
//        this.advancementService.grant(player, "myserver:join");

        // Initialize player advancement manager if not already done
        UUID playerId = player.getUuid();
        PlayerAdvancementManager manager = new PlayerAdvancementManager(player);

        return Result.SUCCESS;
    }
}
