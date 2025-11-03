package nl.devpieter.listeners;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.LobbyPlayer;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.features.mining.MiningManager;
import nl.devpieter.features.mining.tool.ITool;
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

        if (player instanceof LobbyPlayer lobbyPlayer) {
            if (EnvStatics.DEBUG) {
                List<IRod> rods = FishingManager.getInstance().getRods();
                for (IRod rod : rods) lobbyPlayer.addItemToMainInventory(rod.buildItem());

                List<ITool> tools = MiningManager.getInstance().getTools();
                for (ITool tool : tools) lobbyPlayer.addItemToMainInventory(tool.buildItem());
            } else {
                IRod rod = FishingManager.getInstance().getRods().getLast();
                lobbyPlayer.addItemToMainInventory(rod.buildItem());

                ITool tool = MiningManager.getInstance().getTools().getLast();
                lobbyPlayer.addItemToMainInventory(tool.buildItem());
            }
        }

        return Result.SUCCESS;
    }
}
