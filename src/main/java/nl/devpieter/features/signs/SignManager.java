package nl.devpieter.features.signs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.Settings;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.managers.WorldManager;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.utils.WorldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SignManager {

    private static SignManager INSTANCE;

    private static final int SCAN_RADIUS = 24;
    private static final Vec SCAN_CENTER = new Vec(0, 64, 0);
    private static final String MARKED_SIGN_PREFIX = "[PTR]";

    private final ComponentLogger logger = MinecraftServer.LOGGER;

    private final WorldManager worldManager = WorldManager.getInstance();
    private final List<Entity> debugMarkers = new ArrayList<>();

    private final List<SignHolder> signHolders = new ArrayList<>();
    private final HashMap<String, List<SignHolder>> signHolderMap = new HashMap<>();

    private SignManager() {
    }

    public static SignManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SignManager();
        return INSTANCE;
    }

    public void init() {
        clearDebugMarkers();
        revertSigns();

        worldManager.getWorlds().forEach(this::scanSigns);
        if (Settings.DEBUG) placeDebugMarkers();
    }

    public List<SignHolder> getMatchedSigns(String key) {
        return signHolderMap.getOrDefault(key, new ArrayList<>());
    }

    private void scanSigns(UUID uuid, Instance instance) {
        logger.info("Scanning instance {} for marked signs...", uuid);
        List<Point> locations = new ArrayList<>();

        locations.addAll(WorldUtils.findBlocks(instance, SCAN_CENTER, SCAN_RADIUS, Block.OAK_SIGN));
        locations.addAll(WorldUtils.findBlocks(instance, SCAN_CENTER, SCAN_RADIUS, Block.OAK_WALL_SIGN));
        locations.addAll(WorldUtils.findBlocks(instance, SCAN_CENTER, SCAN_RADIUS, Block.OAK_HANGING_SIGN));
        locations.addAll(WorldUtils.findBlocks(instance, SCAN_CENTER, SCAN_RADIUS, Block.OAK_WALL_HANGING_SIGN));

        if (locations.isEmpty()) {
            logger.info("No signs found in the instance {}.", uuid);
            return;
        }

        for (Point point : locations) {
            Block block = instance.getBlock(point);
            BlockHolder blockHolder = new BlockHolder(instance, point, block);

            SignHolder signHolder = SignHelper.readSign(blockHolder);
            if (signHolder == null) continue;

            String firstLine = signHolder.lines().getFirst();
            if (!firstLine.startsWith(MARKED_SIGN_PREFIX)) continue;

            signHolders.add(signHolder);

            String cleanedFirstLine = firstLine.substring(MARKED_SIGN_PREFIX.length()).trim();
            signHolderMap.computeIfAbsent(cleanedFirstLine, k -> new ArrayList<>()).add(signHolder);

            instance.setBlock(point, Block.AIR);
        }

        logger.info("Found {} marked signs in instance {}.", signHolders.size(), uuid);
    }

    private void revertSigns() {
        if (signHolders.isEmpty() && signHolderMap.isEmpty()) return;
        logger.info("Reverting {} signs to their original state.", signHolders.size());

        for (SignHolder signHolder : signHolders) {
            signHolder.block().instance().setBlock(
                    signHolder.block().position(),
                    signHolder.block().block()
            );
        }
        signHolders.clear();
        signHolderMap.clear();
    }

    private void placeDebugMarkers() {
        for (SignHolder signHolder : signHolders) {
            BlockHolder block = signHolder.block();

            String cleanedFirstLine = signHolder.lines().getFirst().substring(MARKED_SIGN_PREFIX.length()).trim();
            signHolderMap.computeIfAbsent(cleanedFirstLine, k -> new ArrayList<>()).add(signHolder);

            debugMarkers.add(WorldUtils.placeMarker(
                    block.instance(),
                    block.position().add(0.5, 0.5, 0.5),
                    Component.text("Marked | " + cleanedFirstLine)
            ));
        }
    }

    private void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }
}
