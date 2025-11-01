package nl.devpieter.features.parkour.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.models.parkour.Parkour;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import org.jetbrains.annotations.NotNull;

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

        LoadedWorld world = worldManager.getLoadedWorldByInstance(instance);
        if (world == null) return Result.SUCCESS;

        Parkour parkour = this.parkourManager.getParkour(world.world().id(), player);
        if (parkour != null) this.parkourManager.startParkour(player, parkour.id());
        this.parkourManager.checkNode(player);
        this.parkourManager.checkFinish(player);

        return Result.SUCCESS;
    }
}
