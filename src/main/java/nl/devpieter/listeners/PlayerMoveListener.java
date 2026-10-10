package nl.devpieter.listeners;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.features.world.WorldManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import space.pietr.lobby.common.features.world.LoadedWorld;
import space.pietr.lobby.common.features.world.settings.WorldBoundsSetting;
import space.pietr.lobby.common.models.simple.SimpleRange;
import space.pietr.lobby.common.utils.minestom.PlayerUtils;

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

        if (packManager.canAutoPrompt(player, PackManager.MAIN_PACK)) {
            LoadedWorld world = worldManager.getLoadedWorldByInstance(player.getInstance());

            if (world != null) {
                Pos spawn = world.world().settings().spawn().toPoint().asPos();
                if (spawn.distance(player.getPosition()) >= 3) packManager.tryAutoPrompt(player, PackManager.MAIN_PACK);
            }
        }

        if (player.getGameMode() == GameMode.CREATIVE) return Result.SUCCESS;
        Pos position = player.getPosition();

        if (position.y() <= 35 && parkourManager.isDoingParkour(player)) {
            parkourManager.returnToCheckpoint(player, ParkourFailReason.PlayerFell);
            return Result.SUCCESS;
        }

        LoadedWorld world = worldManager.getLoadedWorldByInstance(player.getInstance());
        if (world == null) return Result.INVALID;

        WorldBoundsSetting bounds = world.world().settings().bounds();

        if (!checkXBounds(position, bounds.x())) {
            returnToSpawn(player, world);
            return Result.SUCCESS;
        }

        if (!checkYBounds(position, bounds.y())) {
            returnToSpawn(player, world);
            return Result.SUCCESS;
        }

        if (!checkZBounds(position, bounds.z())) {
            returnToSpawn(player, world);
            return Result.SUCCESS;
        }

        return Result.SUCCESS;
    }

    private void returnToSpawn(@NonNull Player player, @NonNull LoadedWorld world) {
        Pos spawn = world.world().settings().spawn().toPoint().asPos();
        PlayerUtils.teleport(player, spawn);
    }

    private boolean checkXBounds(@NonNull Pos position, @Nullable SimpleRange xRange) {
        if (xRange == null) return true;
        return position.x() >= xRange.min() && position.x() <= xRange.max();
    }

    private boolean checkYBounds(@NonNull Pos position, @Nullable SimpleRange yRange) {
        if (yRange == null) return true;
        return position.y() >= yRange.min() && position.y() <= yRange.max();
    }

    private boolean checkZBounds(@NonNull Pos position, @Nullable SimpleRange zRange) {
        if (zRange == null) return true;
        return position.z() >= zRange.min() && position.z() <= zRange.max();
    }
}
