package nl.devpieter.features.sitting.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerInputEvent;
import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.features.sitting.SitManager;
import org.jetbrains.annotations.NotNull;

public class PlayerStartSneakingListener implements EventListener<@NotNull PlayerInputEvent> {

    private final SitManager sitManager = SitManager.getInstance();

    @Override
    public @NotNull Class<PlayerInputEvent> eventType() {
        return PlayerInputEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerInputEvent event) {
        if (!event.hasPressedShiftKey()) return Result.SUCCESS;
        if (!(event.getPlayer() instanceof LobbyPlayer player)) return Result.SUCCESS;

        sitManager.standPlayer(player);
        return Result.SUCCESS;
    }
}
