package nl.devpieter.features.parkour.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.models.Parkour;
import nl.devpieter.managers.WorldManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ParkourPlayerMoveListener implements EventListener<@NotNull PlayerMoveEvent> {

    private final WorldManager worldManager = WorldManager.getInstance();
    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        if (instance == null) return Result.SUCCESS;

        UUID worldId = worldManager.getWorldUUID(instance);
        if (worldId == null) return Result.SUCCESS;

        Parkour parkour = this.parkourManager.getParkour(worldId, player);
        if (parkour != null) this.parkourManager.startParkour(player, parkour.id());
        this.parkourManager.checkNode(player);
        this.parkourManager.checkFinish(player);

        return Result.SUCCESS;
    }
}
