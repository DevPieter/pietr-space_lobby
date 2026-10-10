package nl.devpieter.features.signs;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.Lobby;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.sees.Sees;
import space.pietr.lobby.api.features.signs.ISignManager;
import space.pietr.lobby.api.features.signs.events.MarkedSignBatchFoundEvent;
import space.pietr.lobby.common.features.signs.SignHelper;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.world.LoadedWorld;
import space.pietr.lobby.common.features.world.settings.WorldMarkerSignsSetting;
import space.pietr.lobby.common.features.world.settings.WorldSettings;
import space.pietr.lobby.common.models.holder.block.IBlockHolder;
import space.pietr.lobby.common.models.simple.SimpleVec;
import space.pietr.lobby.common.utils.minestom.TextUtils;
import space.pietr.lobby.common.utils.minestom.WorldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SignManager implements ISignManager {

    private static final SignManager INSTANCE = new SignManager();
    private static final String MARKED_SIGN_PREFIX = "[PTR]";

    private final ComponentLogger logger = Lobby.getLogger(this.getClass());
    private final Sees sees = Sees.getSharedInstance();

    private final WorldManager worldManager = WorldManager.getInstance();
    private final List<Entity> debugMarkers = new ArrayList<>();

    private final List<SignHolder> signHolders = new ArrayList<>();
    private final HashMap<String, List<SignHolder>> signHolderMap = new HashMap<>();

    private SignManager() {
    }

    public static SignManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        revertSigns();

        worldManager.getLoadedWorlds().forEach(this::scanSigns);
        if (EnvStatics.DEBUG) placeDebugMarkers();
    }

    private void scanSigns(LoadedWorld world) {
        String id = world.world().id();
        Instance instance = world.instance();

        logger.info("Scanning instance {} for marked signs...", id);

        WorldSettings settings = world.world().settings();
        WorldMarkerSignsSetting markerSettings = settings.markerSigns();

        if (!markerSettings.enabled()) {
            logger.info("Marker signs are disabled for instance {}.", id);
            return;
        }

        SimpleVec centerSetting = markerSettings.center();
        if (centerSetting == null) {
            logger.info("No center defined for marker signs in instance {}.", id);
            return;
        }

        Vec scanCenter = centerSetting.toVec();
        int scanRadius = markerSettings.scanRadius();

        if (scanRadius <= 0) {
            logger.info("Invalid scan radius for marker signs in instance {}.", id);
            return;
        }

        List<BlockVec> locations = WorldUtils.findBlocks(instance, scanCenter, scanRadius,
                Block.OAK_SIGN,
                Block.OAK_WALL_SIGN,
                Block.OAK_HANGING_SIGN,
                Block.OAK_WALL_HANGING_SIGN
        );

        if (locations.isEmpty()) {
            logger.info("No signs found in the instance {}.", id);
            return;
        }

        int signsFound = handleSigns(instance, locations);
        logger.info("Found {} marked signs in instance {}.", signsFound, id);
    }

    @Override
    public void scanSigns(Instance instance, BlockVec origin, BoundingBox box) {
        logger.info("Scanning instance for marked signs in the specified area...");

        List<BlockVec> locations = WorldUtils.findBlocks(instance, origin, box,
                Block.OAK_SIGN,
                Block.OAK_WALL_SIGN,
                Block.OAK_HANGING_SIGN,
                Block.OAK_WALL_HANGING_SIGN
        );

        if (locations.isEmpty()) {
            logger.info("No signs found in the specified area.");
            return;
        }

        int signsFound = handleSigns(instance, locations);
        logger.info("Found {} marked signs in the specified area.", signsFound);
    }

    private int handleSigns(Instance instance, List<BlockVec> locations) {
        HashMap<String, List<SignHolder>> newSignHolderMap = new HashMap<>();
        int signsFound = 0;

        for (BlockVec blockVec : locations) {
            Block block = instance.getBlock(blockVec);
            IBlockHolder blockHolder = new IBlockHolder(instance, blockVec, block);

            SignHolder signHolder = SignHelper.readSign(blockHolder);
            if (signHolder == null) continue;

            String firstLine = signHolder.lines().getFirst();
            if (!firstLine.startsWith(MARKED_SIGN_PREFIX)) continue;

            signHolders.add(signHolder);
            signsFound++;

            String cleanedFirstLine = firstLine.substring(MARKED_SIGN_PREFIX.length()).trim();

            signHolderMap.computeIfAbsent(cleanedFirstLine, _ -> new ArrayList<>()).add(signHolder);
            newSignHolderMap.computeIfAbsent(cleanedFirstLine, _ -> new ArrayList<>()).add(signHolder);

            instance.setBlock(blockVec, Block.AIR);
        }

        for (String key : newSignHolderMap.keySet()) {
            sees.dispatch(new MarkedSignBatchFoundEvent(key, newSignHolderMap.get(key)));
        }

        return signsFound;
    }

    public void revertSigns() {
        clearDebugMarkers();

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

    public void placeDebugMarkers() {
        clearDebugMarkers();

        for (SignHolder signHolder : signHolders) {
            IBlockHolder block = signHolder.block();

            String cleanedFirstLine = signHolder.lines().getFirst().substring(MARKED_SIGN_PREFIX.length()).trim();

            debugMarkers.add(WorldUtils.placeMarker(
                    block.instance(),
                    block.position().add(0.5, 0.5, 0.5),
                    TextUtils.miniText("<#4bafff>Marked <gray>%s", cleanedFirstLine)));
        }
    }

    public void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }

    @Override
    public List<SignHolder> getMatchedSigns(String key) {
        return List.of();
    }
}
