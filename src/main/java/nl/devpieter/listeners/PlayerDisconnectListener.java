package nl.devpieter.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import nl.devpieter.managers.SitManager;
import org.jetbrains.annotations.NotNull;

public class PlayerDisconnectListener implements EventListener<@NotNull PlayerDisconnectEvent> {

    private final SitManager sitManager = SitManager.getInstance();

    @Override
    public @NotNull Class<PlayerDisconnectEvent> eventType() {
        return PlayerDisconnectEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerDisconnectEvent event) {
        this.sitManager.standPlayer(event.getPlayer());
        return Result.SUCCESS;
    }
}
