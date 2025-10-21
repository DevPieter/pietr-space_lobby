package nl.devpieter.features.portals;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.Settings;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.managers.WorldManager;
import nl.devpieter.utils.WorldUtils;
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
                UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"),
                List.of(new Portal(
                        UUID.fromString("aba23b3b-0cae-42dd-92a0-7aaf63f4fe54"),
                        new Pos(2.5, 58, -2.5, -180, 0),
                        new Pos(4, 61, -2),
                        new Pos(1, 58, -2)
                ))
        );

        this.portals.put(
                UUID.fromString("aba23b3b-0cae-42dd-92a0-7aaf63f4fe54"),
                List.of(new Portal(
                        UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"),
                        new Pos(2.5, 58, -2.5, -180, 0),
                        new Pos(4, 61, -2),
                        new Pos(1, 58, -2)
                ))
        );
    }

    public static PortalManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PortalManager();
        return INSTANCE;
    }

    public void init() {
        if (!Settings.DEBUG) return;

        for (UUID worldUuid : this.portals.keySet()) {
            InstanceContainer world = WorldManager.getInstance().getWorld(worldUuid);
            if (world == null) continue;

            for (Portal portal : this.portals.get(worldUuid)) {
                BoundingBox box = new BoundingBox(
                        new Vec(portal.start().x(), portal.start().y(), portal.start().z()),
                        new Vec(portal.end().x(), portal.end().y(), portal.end().z())
                );

                WorldUtils.placeBoundingBox(world, new Pos(0, 0, 0), box);
            }
        }
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
