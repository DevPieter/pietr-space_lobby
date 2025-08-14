package nl.devpieter.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerStartSneakingEvent;
import nl.devpieter.managers.SitManager;
import org.jetbrains.annotations.NotNull;

public class PlayerStartSneakingListener implements EventListener<@NotNull PlayerStartSneakingEvent> {

    private final SitManager sitManager = SitManager.getInstance();

    @Override
    public @NotNull Class<PlayerStartSneakingEvent> eventType() {
        return PlayerStartSneakingEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerStartSneakingEvent event) {
        this.sitManager.standPlayer(event.getPlayer());
        return Result.SUCCESS;
    }
}
