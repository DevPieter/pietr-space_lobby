package nl.devpieter.listeners;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.utils.minestom.PlayerUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerMoveListener implements EventListener<@NotNull PlayerMoveEvent> {

    private final PackManager packManager = PackManager.getInstance();
    private final WorldManager worldManager = WorldManager.getInstance();
    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (packManager.canAutoPrompt(player)) {
            LoadedWorld world = worldManager.getLoadedWorldByInstance(player.getInstance());

            if (world != null) {
                Pos spawn = world.world().settings().spawn().toPoint().asPos();
                if (spawn.distance(player.getPosition()) >= 3) packManager.tryAutoPrompt(player);
            }
        }

        if (player.getPosition().y() > 35) return Result.SUCCESS;

        if (this.parkourManager.isDoingParkour(player)) {
            this.parkourManager.returnToCheckpoint(player, ParkourFailReason.PlayerFell);
            return Result.SUCCESS;
        }

        LoadedWorld world = worldManager.getLoadedWorldByInstance(player.getInstance());
        if (world == null) return Result.INVALID;

        Pos spawn = world.world().settings().spawn().toPoint().asPos();
        PlayerUtils.teleport(player, spawn);

        return Result.SUCCESS;
    }
}
