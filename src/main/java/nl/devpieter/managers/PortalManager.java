package nl.devpieter.managers;

import net.minestom.server.coordinate.Pos;
import nl.devpieter.models.Portal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PortalManager {

    private static final double CHECK_RADIUS = 0.5;

    private static PortalManager INSTANCE;

    private final HashMap<UUID, List<Portal>> portals = new HashMap<>();
    private final HashMap<UUID, Long> portalCooldowns = new HashMap<>();

    private PortalManager() {
        this.portals.put(
                WorldManager.getInstance().getMainWorld().getUuid(),
                List.of(new Portal(
                        WorldManager.getInstance().getSpecialWorld().getUuid(),
                        new Pos(2.5, 58, -2.5, -180, 0),
                        new Pos(3, 60, -2),
                        new Pos(1, 58, -2)
                ))
        );

        this.portals.put(
                WorldManager.getInstance().getSpecialWorld().getUuid(),
                List.of(new Portal(
                        WorldManager.getInstance().getMainWorld().getUuid(),
                        new Pos(2.5, 58, -2.5, -180, 0),
                        new Pos(3, 60, -2),
                        new Pos(1, 58, -2)
                ))
        );
    }

    public static PortalManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PortalManager();
        return INSTANCE;
    }

    public List<Portal> getPortalsInWorld(UUID world) {
        if (!this.portals.containsKey(world)) return List.of();
        return this.portals.get(world);
    }

    public @Nullable Portal getIntersectingPortal(@NotNull Pos pos, @NotNull UUID world) {
        List<Portal> portals = this.getPortalsInWorld(world);

        for (Portal portal : portals) {
            if (!isBetweenCoordinates(pos, portal.start(), portal.end(), CHECK_RADIUS)) continue;
            return portal;
        }

        return null;
    }

    public void startPortalCooldown(@NotNull UUID playerUuid) {
        this.portalCooldowns.put(playerUuid, System.currentTimeMillis());
    }

    public boolean isPortalOnCooldown(@NotNull UUID playerUuid) {
        if (!this.portalCooldowns.containsKey(playerUuid)) return false;

        long cooldownTime = this.portalCooldowns.get(playerUuid);
        long currentTime = System.currentTimeMillis();

        return (currentTime - cooldownTime) < 3000;
    }

    private static boolean isBetweenCoordinates(@NotNull Pos pos, @NotNull Pos start, @NotNull Pos end, double tolerance) {
        double minX = Math.min(start.x(), end.x()) - tolerance / 2;
        double maxX = Math.max(start.x(), end.x()) + tolerance / 2;

        double minY = Math.min(start.y(), end.y()) - tolerance / 2;
        double maxY = Math.max(start.y(), end.y()) + tolerance / 2;

        double minZ = Math.min(start.z(), end.z()) - tolerance / 2;
        double maxZ = Math.max(start.z(), end.z()) + tolerance / 2;

        return pos.x() >= minX && pos.x() <= maxX &&
                pos.y() >= minY && pos.y() <= maxY &&
                pos.z() >= minZ && pos.z() <= maxZ;
    }
}
