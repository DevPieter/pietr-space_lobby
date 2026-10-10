package nl.devpieter.features.portals;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import nl.devpieter.Lobby;
import space.pietr.lobby.common.EnvStatics;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.portals.models.PortalPointHolder;
import nl.devpieter.features.signs.SignManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.pietr.lobby.common.features.signs.SignDecoder;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.models.holder.IPointHolder;
import space.pietr.lobby.common.utils.minestom.TextUtils;
import space.pietr.lobby.common.utils.minestom.WorldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PortalManager {

    private static PortalManager INSTANCE;
    private static final String SIGN_KEY = "<PORTAL>";

    private final HashMap<UUID, Long> portalCooldowns = new HashMap<>();

    private final ComponentLogger logger = Lobby.getLogger(this.getClass());
    private final SignManager signManager = SignManager.getInstance();

    private final List<Entity> debugMarkers = new ArrayList<>();

    private final List<PortalPointHolder> portalLocations = new ArrayList<>();
    private final List<Portal> portals = new ArrayList<>();

    public static PortalManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PortalManager();
        return INSTANCE;
    }

    public void init() {
        revertPortals();
        scanAndRegisterPortals();

        if (EnvStatics.DEBUG) placeDebugMarkers();
    }

    private void scanAndRegisterPortals() {
        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {
            PortalDecodable portalDecodable = new PortalDecodable();

            List<String> lines = sign.lines().subList(1, sign.lines().size());
            SignDecoder.decodeLines(lines, portalDecodable.getDecoders());

            Portal portal = portalDecodable.placePortal(sign);
            portals.add(portal);

            portalLocations.add(new PortalPointHolder(
                    portal.destinationWorld().id(), IPointHolder.of(sign.block())
            ));
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

        for (PortalPointHolder holder : portalLocations) {
            debugMarkers.add(WorldUtils.placeMarker(
                    holder.point().instance(),
                    holder.point().position().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#a84bff>Portal <gray>%s", holder.identifier())
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

    // TODO - Move to Utils class
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
