package nl.devpieter.features.portals;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import nl.devpieter.Settings;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.signs.SignDecoder;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.TextUtils;
import nl.devpieter.utils.WorldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PortalManager {

    private static PortalManager INSTANCE;
    private static final String SIGN_KEY = "<PORTAL>";

    private final HashMap<UUID, Long> portalCooldowns = new HashMap<>();

    private final ComponentLogger logger = MinecraftServer.LOGGER;
    private final SignManager signManager = SignManager.getInstance();

    private final List<Entity> debugMarkers = new ArrayList<>();

    private final List<PointHolder> portalLocations = new ArrayList<>();
    private final List<Portal> portals = new ArrayList<>();

    public static PortalManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PortalManager();
        return INSTANCE;
    }

    public void init() {
        revertPortals();
        scanAndRegisterPortals();

        if (Settings.DEBUG) placeDebugMarkers();
    }

    private void scanAndRegisterPortals() {
        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {
            PortalDecodable portalDecodable = new PortalDecodable();

            List<String> lines = sign.lines().subList(1, sign.lines().size());
            SignDecoder.decodeLines(lines, portalDecodable.getDecoders());

            portals.add(portalDecodable.placePortal(
                    sign.block().instance(),
                    sign.block().position(),
                    sign
            ));

            portalLocations.add(PointHolder.of(sign.block()));
        }
    }

    public void revertPortals() {
        clearDebugMarkers();

        if (portals.isEmpty()) return;
        logger.info("Reverting {} portals to their original state.", portals.size());

        portals.clear();
        portalLocations.clear();
    }

    public @Nullable Portal getIntersectingPortal(@NotNull Player player) {
        for (Portal portal : portals) {
            if (portal.instance() != player.getInstance()) continue;

            if (!isInsideBox(player, portal.origin(), portal.boundingBox())) continue;
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

    public void placeDebugMarkers() {
        clearDebugMarkers();

        for (PointHolder point : portalLocations) {
            debugMarkers.add(WorldUtils.placeMarker(
                    point.instance(),
                    point.position().add(0.5, 0.8, 0.5),
                    TextUtils.miniText("<#a84bff>Portal")
            ));
        }

        for (Portal portal : portals) {
            debugMarkers.add(WorldUtils.placeBoundingBox(
                    portal.instance(),
                    portal.origin(),
                    portal.boundingBox()
            ));
        }
    }

    public void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }

//    private static boolean isBetweenCoordinates(@NotNull Point pos, @NotNull Point start, @NotNull Point end, double tolerance) {
//        double minX = Math.min(start.x(), end.x()) - tolerance / 2;
//        double maxX = Math.max(start.x(), end.x()) + tolerance / 2;
//
//        double minY = Math.min(start.y(), end.y()) - tolerance / 2;
//        double maxY = Math.max(start.y(), end.y()) + tolerance / 2;
//
//        double minZ = Math.min(start.z(), end.z()) - tolerance / 2;
//        double maxZ = Math.max(start.z(), end.z()) + tolerance / 2;
//
//        return pos.x() >= minX && pos.x() <= maxX &&
//                pos.y() >= minY && pos.y() <= maxY &&
//                pos.z() >= minZ && pos.z() <= maxZ;
//    }

    public static boolean isInsideBox(@NotNull Player player, @NotNull Point origin, @NotNull BoundingBox box) {
        BoundingBox playerBB = player.getBoundingBox();
        Pos playerPos = player.getPosition();

        double halfWidth = playerBB.width() / 2;
        double height = playerBB.height();

        double playerMinX = playerPos.x() - halfWidth;
        double playerMaxX = playerPos.x() + halfWidth;
        double playerMinY = playerPos.y();
        double playerMaxY = playerPos.y() + height;
        double playerMinZ = playerPos.z() - halfWidth;
        double playerMaxZ = playerPos.z() + halfWidth;

        double boxMinX = origin.x() + Math.min(box.relativeStart().x(), box.relativeEnd().x());
        double boxMaxX = origin.x() + Math.max(box.relativeStart().x(), box.relativeEnd().x());

        double boxMinY = origin.y() + Math.min(box.relativeStart().y(), box.relativeEnd().y());
        double boxMaxY = origin.y() + Math.max(box.relativeStart().y(), box.relativeEnd().y());

        double boxMinZ = origin.z() + Math.min(box.relativeStart().z(), box.relativeEnd().z());
        double boxMaxZ = origin.z() + Math.max(box.relativeStart().z(), box.relativeEnd().z());

        return (playerMinX < boxMaxX && playerMaxX > boxMinX) &&
                (playerMinY < boxMaxY && playerMaxY > boxMinY) &&
                (playerMinZ < boxMaxZ && playerMaxZ > boxMinZ);
    }
}
