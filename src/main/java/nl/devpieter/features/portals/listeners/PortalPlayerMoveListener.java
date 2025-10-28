package nl.devpieter.features.portals.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.portals.PortalManager;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.utils.minestom.PlayerUtils;
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

        LoadedWorld world = this.worldManager.getLoadedWorldByInstance(currentWorld);
        if (world == null) return Result.SUCCESS;

        Portal portal = this.portalManager.getIntersectingPortal(player);
        if (portal == null) return Result.SUCCESS;

        LoadedWorld destination = this.worldManager.getLoadedWorld(portal.destinationWorld());
        if (destination == null) return Result.SUCCESS;

        this.parkourManager.cancelParkour(player, ParkourCancelReason.PortalUsed);

        this.portalManager.startPortalCooldown(player.getUuid());

        if (player.getInstance() != destination.instance()) {
            player.setInstance(destination.instance(), portal.destinationPos());
        } else {
            PlayerUtils.teleport(player, portal.destinationPos());
        }

        return Result.SUCCESS;
    }
}
