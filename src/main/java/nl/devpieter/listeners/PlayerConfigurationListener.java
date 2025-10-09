package nl.devpieter.listeners;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.Settings;
import nl.devpieter.managers.WorldManager;
import org.jetbrains.annotations.NotNull;

public class PlayerConfigurationListener implements EventListener<@NotNull AsyncPlayerConfigurationEvent> {

    private final WorldManager worldManager = WorldManager.getInstance();

    @Override
    public @NotNull Class<AsyncPlayerConfigurationEvent> eventType() {
        return AsyncPlayerConfigurationEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull AsyncPlayerConfigurationEvent event) {
        InstanceContainer container = worldManager.getMainWorld();
        event.setSpawningInstance(container);

        Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        player.setRespawnPoint(Settings.MAIN_WORLD_SPAWN);

        player.getInventory().addItemStack(ItemStack.of(Material.FISHING_ROD));

        return Result.SUCCESS;
    }
}
