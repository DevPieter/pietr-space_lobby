package nl.devpieter.features.portals;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.Settings;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.utils.WorldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PortalManager {

    private static final double CHECK_RADIUS = 0.5;

    private static PortalManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();

    private final HashMap<String, List<Portal>> portals = new HashMap<>();
    private final HashMap<UUID, Long> portalCooldowns = new HashMap<>();

    private PortalManager() {
        this.portals.put(
                "main",
                List.of(new Portal(
                        "special",
                        new Pos(2.5, 58, -2.5, -180, 0),
                        new Pos(4, 61, -2),
                        new Pos(1, 58, -2)
                ))
        );

        this.portals.put(
                "special",
                List.of(new Portal(
                        "main",
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

        for (String worldId : this.portals.keySet()) {
            LoadedWorld world = worldManager.getWorldById(worldId);
            if (world == null) continue;

            InstanceContainer instance = world.instance();

            for (Portal portal : this.portals.get(worldId)) {
                BoundingBox box = new BoundingBox(
                        new Vec(portal.start().x(), portal.start().y(), portal.start().z()),
                        new Vec(portal.end().x(), portal.end().y(), portal.end().z())
                );

                WorldUtils.placeBoundingBox(instance, new Pos(0, 0, 0), box);
            }
        }
    }

    public List<Portal> getPortalsInWorld(String world) {
        if (!this.portals.containsKey(world)) return List.of();
        return this.portals.get(world);
    }

    public @Nullable Portal getIntersectingPortal(@NotNull Pos pos, @NotNull String world) {
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
