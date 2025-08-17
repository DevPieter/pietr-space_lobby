package nl.devpieter.listeners.portal;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.enums.ParkourCancelReason;
import nl.devpieter.managers.ParkourManager;
import nl.devpieter.managers.PortalManager;
import nl.devpieter.managers.WorldManager;
import nl.devpieter.models.Portal;
import org.jetbrains.annotations.NotNull;

public class PortalPlayerMoveListener implements EventListener<@NotNull PlayerMoveEvent> {

    private final WorldManager worldManager = WorldManager.getInstance();
    private final PortalManager portalManager = PortalManager.getInstance();
    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Instance currentWorld = player.getInstance();
        if (currentWorld == null) return Result.SUCCESS;

        if (this.portalManager.isPortalOnCooldown(player.getUuid())) {
            return Result.SUCCESS;
        }

        Portal portal = this.portalManager.getIntersectingPortal(player.getPosition(), currentWorld.getUuid());
        if (portal == null) return Result.SUCCESS;

        InstanceContainer destination = this.worldManager.getWorld(portal.destinationWorld());
        if (destination == null) return Result.SUCCESS;

        this.parkourManager.cancelParkour(player, ParkourCancelReason.PortalUsed);

        this.portalManager.startPortalCooldown(player.getUuid());
        player.setInstance(destination, portal.destinationPos());
        return Result.SUCCESS;
    }
}
