package nl.devpieter.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import nl.devpieter.Settings;
import org.jetbrains.annotations.NotNull;

public class PlayerMoveListener implements EventListener<@NotNull PlayerMoveEvent> {

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getPosition().y() > 0) return Result.SUCCESS;

        player.teleport(Settings.MAIN_WORLD_SPAWN);
        return Result.SUCCESS;
    }
}
