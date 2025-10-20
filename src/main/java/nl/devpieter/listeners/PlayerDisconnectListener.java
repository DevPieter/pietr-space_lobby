package nl.devpieter.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import nl.devpieter.features.fishing.listeners.PlayerUseItemListener;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.sitting.SitManager;
import org.jetbrains.annotations.NotNull;

public class PlayerDisconnectListener implements EventListener<@NotNull PlayerDisconnectEvent> {

    private final SitManager sitManager = SitManager.getInstance();
    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerDisconnectEvent> eventType() {
        return PlayerDisconnectEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerDisconnectEvent event) {
        this.sitManager.standPlayer(event.getPlayer());
        this.parkourManager.cancelParkour(event.getPlayer(), ParkourCancelReason.PlayerQuit);

        PlayerUseItemListener.reelInFishingRod(event.getPlayer());
        return Result.SUCCESS;
    }
}
