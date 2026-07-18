package nl.devpieter.features.parkour.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerInputEvent;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import org.jetbrains.annotations.NotNull;

public class ParkourPlayerSneakListener implements EventListener<@NotNull PlayerInputEvent> {

    private final WorldManager worldManager = WorldManager.getInstance();
    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerInputEvent> eventType() {
        return PlayerInputEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerInputEvent event) {
        if (!event.hasPressedShiftKey()) return Result.SUCCESS;

        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        if (instance == null) return Result.SUCCESS;

        LoadedWorld world = worldManager.getLoadedWorldByInstance(instance);
        if (world == null) return Result.SUCCESS;

        parkourManager.useReturnToStart(player);
        return Result.SUCCESS;
    }
}
