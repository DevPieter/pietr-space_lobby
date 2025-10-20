package nl.devpieter.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import nl.devpieter.Settings;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.utils.PlayerUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerMoveListener implements EventListener<@NotNull PlayerMoveEvent> {

    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getPosition().y() > 50) return Result.SUCCESS;

        if (this.parkourManager.isDoingParkour(player)) {
            this.parkourManager.returnToCheckpoint(player, ParkourFailReason.PlayerFell);
            return Result.SUCCESS;
        }

        PlayerUtils.teleport(player, Settings.MAIN_WORLD_SPAWN);
        return Result.SUCCESS;
    }
}
