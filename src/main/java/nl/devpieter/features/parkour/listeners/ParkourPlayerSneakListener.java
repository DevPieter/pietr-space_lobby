package nl.devpieter.features.parkour.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerStartSneakingEvent;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import org.jetbrains.annotations.NotNull;

public class ParkourPlayerSneakListener implements EventListener<@NotNull PlayerStartSneakingEvent> {

    private final WorldManager worldManager = WorldManager.getInstance();
    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerStartSneakingEvent> eventType() {
        return PlayerStartSneakingEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerStartSneakingEvent event) {
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        if (instance == null) return Result.SUCCESS;

        LoadedWorld world = worldManager.getLoadedWorldByInstance(instance);
        if (world == null) return Result.SUCCESS;

        parkourManager.tryReturnToStart(player);
        return Result.SUCCESS;
    }
}
