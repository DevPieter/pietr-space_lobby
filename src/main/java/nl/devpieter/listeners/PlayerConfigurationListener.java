package nl.devpieter.listeners;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerConfigurationListener implements EventListener<@NotNull AsyncPlayerConfigurationEvent> {

    private final WorldManager worldManager = WorldManager.getInstance();

    @Override
    public @NotNull Class<AsyncPlayerConfigurationEvent> eventType() {
        return AsyncPlayerConfigurationEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull AsyncPlayerConfigurationEvent event) {
        LoadedWorld world = worldManager.getLoadedWorldById("main");
        if (world == null) return Result.INVALID;

        InstanceContainer container = world.instance();
        event.setSpawningInstance(container);

        Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        player.setRespawnPoint(world.world().settings().spawn().toPoint().asPos());

        List<IRod> rods = FishingManager.getInstance().getRods();
        for (IRod rod : rods) player.getInventory().addItemStack(rod.buildItem());

        return Result.SUCCESS;
    }
}
