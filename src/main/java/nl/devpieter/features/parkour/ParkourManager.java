package nl.devpieter.features.parkour;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.TagHelper;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.enums.PlayerTags;
import nl.devpieter.features.parkour.decodables.*;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.parkour.models.ParkourReturnHolder;
import nl.devpieter.features.parkour.models.parkour.*;
import nl.devpieter.features.parkour.models.parkourTry.FailedNode;
import nl.devpieter.features.parkour.models.parkourTry.ParkourTry;
import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import nl.devpieter.features.signs.SignDecoder;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.tips.MsgManager;
import nl.devpieter.features.tips.TipManager;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.models.GlowingBlockHolder;
import nl.devpieter.models.SlotHolder;
import nl.devpieter.utils.minestom.PacketUtils;
import nl.devpieter.utils.minestom.PlayerUtils;
import nl.devpieter.utils.minestom.TextUtils;
import nl.devpieter.utils.minestom.WorldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class ParkourManager {

    private static ParkourManager INSTANCE;
    private static final String SIGN_KEY = "<PARKOUR>";

    private final ComponentLogger logger = MinecraftServer.LOGGER;

    private final MsgManager msgManager = MsgManager.getInstance();
    private final TipManager tipManager = TipManager.getInstance();
    private final SignManager signManager = SignManager.getInstance();
    private final WorldManager worldManager = WorldManager.getInstance();

    private final ParkourTryManager parkourTryManager = ParkourTryManager.getInstance();
    private final ParkourReplayManager replayManager = ParkourReplayManager.getInstance();

    private final List<Entity> debugMarkers = new ArrayList<>();
    private final HashMap<String, ParkourSpawnNode> spawnNodes = new HashMap<>();
    private final HashMap<String, ParkourLeaderboardNode> leaderboardNodes = new HashMap<>();
    private final HashMap<String, ParkourStartNode> startNodes = new HashMap<>();
    private final HashMap<String, ParkourEndNode> endNodes = new HashMap<>();
    private final HashMap<String, List<ParkourNode>> normalNodes = new HashMap<>();

    private final HashMap<String, Parkour> parkours = new HashMap<>();
    private final HashMap<String, List<Parkour>> worldParkourLookup = new HashMap<>();

    private final HashMap<UUID, ParkourTry> players = new HashMap<>();
    private final HashMap<UUID, ParkourReturnHolder> returnHolders = new HashMap<>();
    private final HashMap<UUID, GlowingBlockHolder> glowingHolders = new HashMap<>();

    private ParkourManager() {
    }

    public static ParkourManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourManager();
        return INSTANCE;
    }

    public void init() {
        revertParkours();
        scanAndRegisterParkours();

        if (EnvStatics.DEBUG) placeDebugMarkers();
    }

    private void scanAndRegisterParkours() {
        HashMap<String, SignHolder> foundParkours = new HashMap<>();
        List<String> incompleteParkours = new ArrayList<>();

        HashMap<String, Boolean> containsIndex = new HashMap<>();

        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {

            LoadedWorld world = worldManager.getLoadedWorldByInstance(sign.block().instance());
            if (world == null) {
                logger.warn("Sign at {} is in an unknown world, skipping parkour sign", sign.block().position());
                continue;
            }

            Point point = sign.block().position();

            String name = sign.lines().get(1);
            String type = sign.lines().get(2).toLowerCase();

            String identifier = getWorldParkourKey(world, name);
            foundParkours.putIfAbsent(identifier, sign);

            List<String> lines = sign.lines().subList(3, sign.lines().size());

            switch (type) {
                case "spawn" -> {
                    if (spawnNodes.containsKey(identifier)) {
                        logger.warn("Duplicate spawn node for parkour: {} on sign at {}", identifier, point);
                        continue;
                    }

                    ParkourSpawnNodeDecodable decodable = new ParkourSpawnNodeDecodable();
                    SignDecoder.decodeLines(lines, decodable.getDecoders());

                    spawnNodes.put(identifier, decodable.getNode(sign));
                }
                case "leaderboard" -> {
                    if (leaderboardNodes.containsKey(identifier)) {
                        logger.warn("Duplicate leaderboard node for parkour: {} on sign at {}", identifier, point);
                        continue;
                    }

                    ParkourLeaderboardNodeDecodable decodable = new ParkourLeaderboardNodeDecodable();
                    SignDecoder.decodeLines(lines, decodable.getDecoders());

                    leaderboardNodes.put(identifier, decodable.getNode(sign));
                }
                case "start" -> {
                    if (startNodes.containsKey(identifier)) {
                        logger.warn("Duplicate start node for parkour: {} on sign at {}", identifier, point);
                        continue;
                    }

                    ParkourStartNodeDecodable decodable = new ParkourStartNodeDecodable();
                    SignDecoder.decodeLines(lines, decodable.getDecoders());

                    startNodes.put(identifier, decodable.getNode(sign));
                }
                case "node" -> {
                    ParkourNodeDecodable decodable = new ParkourNodeDecodable();
                    SignDecoder.decodeLines(lines, decodable.getDecoders());

                    ParkourNode node = decodable.getNode(sign);

                    String indexKey = String.format("%s:%d", identifier, node.index());
                    if (containsIndex.containsKey(indexKey)) {
                        logger.warn("Duplicate node index {} for parkour: {} on sign at {}", node.index(), identifier, point);
                        continue;
                    }

                    containsIndex.put(indexKey, true);
                    normalNodes.computeIfAbsent(identifier, _ -> new ArrayList<>()).add(node);
                }
                case "end" -> {
                    if (endNodes.containsKey(identifier)) {
                        logger.warn("Duplicate end node for parkour: {} on sign at {}", identifier, point);
                        continue;
                    }

                    ParkourEndNodeDecodable decodable = new ParkourEndNodeDecodable();
                    SignDecoder.decodeLines(lines, decodable.getDecoders());

                    endNodes.put(identifier, decodable.getNode(sign));
                }
                default -> logger.warn("Unknown parkour sign type: {} on sign at {}", type, point);
            }
        }

        for (Map.Entry<String, SignHolder> entry : foundParkours.entrySet()) {
            String identifier = entry.getKey();
            SignHolder sign = entry.getValue();

            if (!startNodes.containsKey(identifier) || !normalNodes.containsKey(identifier) || !endNodes.containsKey(identifier)) {
                incompleteParkours.add(identifier);
                continue;
            }

            LoadedWorld world = worldManager.getLoadedWorldByInstance(sign.block().instance());
            if (world == null) {
                logger.warn("Parkour {} is in an unknown world, skipping parkour", identifier);
                incompleteParkours.add(identifier);
                continue;
            }

            List<ParkourNode> nodesOrdered = normalNodes.get(identifier);
            nodesOrdered.sort(Comparator.comparingInt(ParkourNode::index));

            Parkour parkour = new Parkour(
                    identifier,

                    world.id(),
                    world.instance(),

                    spawnNodes.get(identifier),
                    leaderboardNodes.get(identifier),

                    startNodes.get(identifier),
                    endNodes.get(identifier),

                    nodesOrdered
            );

            initParkour(world, parkour);
        }

        if (!incompleteParkours.isEmpty()) {
            logger.warn("Found {} incomplete parkours: {}", incompleteParkours.size(), String.join(", ", incompleteParkours));
        }
    }

    private void initParkour(@NotNull LoadedWorld world, @NotNull Parkour parkour) {
        Instance instance = world.instance();

        Pos start = parkour.startPos();
        instance.setBlock(start, Block.HEAVY_WEIGHTED_PRESSURE_PLATE);

        Pos end = parkour.endPos();
        instance.setBlock(end, Block.LIGHT_WEIGHTED_PRESSURE_PLATE);

        for (ParkourNode node : parkour.nodes()) {
            Pos point = node.pos();
            if (node.isCheckpoint()) instance.setBlock(point, Block.SPRUCE_PRESSURE_PLATE);
        }

        ParkourLeaderboardNode leaderboardNode = parkour.leaderboardNode();
        if (leaderboardNode != null) {
            Pos leaderboardPos = leaderboardNode.point().asPos().add(0.5, 1, 0.5);

            Component text = TextUtils.combine(
                    TextUtils.miniText("<#fcba03><b>Leaderboard"),
                    Component.newline(),
                    Component.newline(),
                    TextUtils.miniText("<gray><i>Stats are currently <b><red>NOT</red></b> saved!")
            );

            Entity entity = new Entity(EntityType.TEXT_DISPLAY);
            entity.setInstance(instance, leaderboardPos);

            entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
                textDisplayMeta.setText(text);
                textDisplayMeta.setScale(new Vec(0.9, 0.9, 0.9));

                textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
                textDisplayMeta.setHasNoGravity(true);
            });

            parkour.setLeaderboardEntity(entity.acquirable());
        }

        parkours.put(parkour.id(), parkour);
        worldParkourLookup.computeIfAbsent(world.id(), _ -> new ArrayList<>()).add(parkour);
    }

    public void revertParkours() {
        spawnNodes.clear();
        leaderboardNodes.clear();
        startNodes.clear();
        endNodes.clear();
        normalNodes.clear();

        for (Parkour parkour : parkours.values()) {
            Instance instance = parkour.instance();

            Pos start = parkour.startPos();
            instance.setBlock(start, Block.AIR);

            Pos end = parkour.endPos();
            instance.setBlock(end, Block.AIR);

            for (ParkourNode node : parkour.nodes()) {
                Pos point = node.pos();
                if (node.isCheckpoint()) instance.setBlock(point, Block.AIR);
            }

//            Entity leaderboardEntity = parkour.leaderboardEntity();
//            if (leaderboardEntity != null) leaderboardEntity.remove();

            Acquirable<? extends Entity> leaderboardEntityAcquirable = parkour.leaderboardEntity();
            if (leaderboardEntityAcquirable != null) leaderboardEntityAcquirable.sync(Entity::remove);
        }

        parkours.clear();
        worldParkourLookup.clear();

        players.clear();
        returnHolders.clear();
        glowingHolders.clear();
    }

    public void placeDebugMarkers() {
        clearDebugMarkers();

        /* Spawn Nodes */
        for (Map.Entry<String, ParkourSpawnNode> entry : spawnNodes.entrySet()) {
            ParkourSpawnNode node = entry.getValue();

            debugMarkers.add(WorldUtils.placeMarker(
                    node.instance(),
                    node.point().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#4bcffa>Spawn <gray>%s", entry.getKey())
            ));
        }

        /* Leaderboard Nodes */
        for (Map.Entry<String, ParkourLeaderboardNode> entry : leaderboardNodes.entrySet()) {
            ParkourLeaderboardNode node = entry.getValue();

            debugMarkers.add(WorldUtils.placeMarker(
                    node.instance(),
                    node.point().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#ff4b4b>Leaderboard <gray>%s", entry.getKey())
            ));
        }

        /* Start Nodes */
        for (Map.Entry<String, ParkourStartNode> entry : startNodes.entrySet()) {
            ParkourStartNode node = entry.getValue();

            debugMarkers.add(WorldUtils.placeMarker(
                    node.instance(),
                    node.point().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#4bff66>Start <gray>%s", entry.getKey())
            ));
        }

        /* End Nodes */
        for (Map.Entry<String, ParkourEndNode> entry : endNodes.entrySet()) {
            ParkourEndNode node = entry.getValue();

            debugMarkers.add(WorldUtils.placeMarker(
                    node.instance(),
                    node.point().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#ffb84b>End <gray>%s", entry.getKey())
            ));
        }

        /* Normal Nodes */
        for (Map.Entry<String, List<ParkourNode>> entry : normalNodes.entrySet()) {
            String parkourId = entry.getKey();
            List<ParkourNode> nodes = entry.getValue();

            for (ParkourNode node : nodes) {
                Pos point = node.pos();

                debugMarkers.add(WorldUtils.placeMarker(
                        node.instance(),
                        point.add(0, 0.7, 0),
                        TextUtils.miniText(node.isCheckpoint() ?
                                        "<#ffa84b>Checkpoint <gray>%s %d" :
                                        "<#ffffff>Node <gray>%s %d",
                                parkourId,
                                node.index()
                        )
                ));
            }
        }
    }

    public void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }

    private String getWorldParkourKey(@NotNull LoadedWorld world, @NotNull String parkourName) {
        return String.format("%s:%s", world.id(), parkourName);
    }

    public boolean isDoingParkour(@NotNull Player player) {
        return players.containsKey(player.getUuid());
    }

    private @Nullable Parkour getParkour(@NotNull String worldParkourKey) {
        return parkours.get(worldParkourKey);
    }

    public @Nullable Parkour getParkourAtPlayerPosition(@NotNull String world, @NotNull Player player) {
        List<Parkour> parkoursInWorld = worldParkourLookup.get(world);
        if (parkoursInWorld == null) return null;

        for (Parkour parkour : parkoursInWorld) {
            if (PlayerUtils.isInsideBox(player, parkour.startPos(), parkour.startBox())) {
                return parkour;
            }
        }

        return null;
    }

    private @NotNull Pos getFirstCheckpoint(@NotNull Parkour parkour) {
        List<ParkourNode> nodes = parkour.nodes();
        for (ParkourNode node : nodes) {
            if (node.isCheckpoint()) return node.pos();
        }

        return parkour.endPos();
    }

    private @Nullable Pos getNextCheckpoint(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return null;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return null;

        int lastReachedIndex = parkourTry.lastReachedNode();
        if (lastReachedIndex < 0) {
            return getFirstCheckpoint(parkour);
        }

        if (lastReachedIndex >= parkour.nodes().size()) {
            return parkour.endPos();
        }

        for (int i = lastReachedIndex + 1; i < parkour.nodes().size(); i++) {
            ParkourNode node = parkour.nodes().get(i);
            if (node.isCheckpoint()) return node.pos();
        }

        return parkour.endPos();
    }

    public void startParkour(@NotNull LobbyPlayer player, @NotNull String parkourUuid) {
        if (players.containsKey(player.getUuid())) return;

        Parkour parkour = parkours.get(parkourUuid);
        if (parkour == null) return;

        if (!TagHelper.canStartParkour(player)) {
            return;
        }

        if (!player.addTag(PlayerTags.IN_PARKOUR)) {
            return;
        }

        invalidateReturnToStart(player);

        player.sendMessage(TextUtils.miniText("<green>Parkour started! Go go go!"));
        player.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1.0f, 1.0f));

        Pos nextCheckpoint = getFirstCheckpoint(parkour);
        glowingHolders.put(player.getUuid(), PacketUtils.replaceWithGlowing(player, nextCheckpoint));
        PacketUtils.setWaypoint(player, parkourUuid, nextCheckpoint);

        player.fillDynamicInventory(
                new SlotHolder(0, ItemStack.of(Material.BARRIER)),
                new SlotHolder(4, ItemStack.of(Material.SPRUCE_PRESSURE_PLATE))
        );
        player.switchToDynamicInventory();


        players.put(player.getUuid(), new ParkourTry(
                parkourUuid,
                player.getUuid()
        ));

        replayManager.startTracking(player);
    }

    public void cancelParkour(@NotNull Player player, ParkourCancelReason reason) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        if (reason == ParkourCancelReason.PlayerRequest) {
            player.sendMessage(TextUtils.miniText("<red>You cancelled the parkour!"));
            player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1.0f, 1.0f));

            if (parkourTry.lastReachedNode() >= 0 && parkourTry.lastReachedNode() < parkour.nodes().size()) {
                ParkourNode lastReachedNode = parkour.nodes().get(parkourTry.lastReachedNode());
                parkourTry.failedNodes().add(FailedNode.create(lastReachedNode.index(), ParkourFailReason.Cancelled));
            }
        } else {
            player.sendMessage(TextUtils.miniText("<red>Parkour cancelled, please don't do that!"));
        }

        parkourTry.cancel(reason);
        handleCleanup(player, parkour, parkourTry);
    }

    public void returnToCheckpoint(@NotNull Player player, @NotNull ParkourFailReason reason) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        int lastReachedIndex = parkourTry.lastCheckpoint();
        ParkourNode currentCheckpointNode = null;

        if (lastReachedIndex >= 0 && lastReachedIndex < parkour.nodes().size()) {
            currentCheckpointNode = parkour.nodes().get(lastReachedIndex);

            if (!currentCheckpointNode.isCheckpoint()) {
                // TODO - Log this as a warning, this should not happen
                currentCheckpointNode = null;
            }
        }

        if (currentCheckpointNode == null) {
            parkourTry.resetProgress();
            PlayerUtils.teleport(player, parkour.startPos());
        } else {
            parkourTry.rollbackToLastCheckpoint();
            PlayerUtils.teleport(player, currentCheckpointNode.pos());
        }

        player.sendActionBar(TextUtils.miniText("<red>You failed the jump!"));
        player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1.0f, 1.0f));

        if (lastReachedIndex < 0 || lastReachedIndex >= parkour.nodes().size()) return;

        ParkourNode lastReachedNode = parkour.nodes().get(lastReachedIndex);
        parkourTry.failedNodes().add(FailedNode.create(lastReachedNode.index(), reason));
    }

    public void checkNode(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        ParkourNode node = getNodeAtPosition(player, parkour);
        if (node == null) return;

        List<ParkourNode> nodes = parkour.nodes();
        int expectedIndex = parkourTry.lastReachedNode() + 1;

        int i = node.index();
        if (parkourTry.isReached(i)) return;

        if (i == expectedIndex) {
            handlePointReached(player, parkourTry, node, i);
            return;
        }

        if (i > expectedIndex) {
            if (nodes.subList(expectedIndex, i).stream().anyMatch(ParkourNode::isCheckpoint)) {
                msgManager.showMessage(player, "parkour_must_reach_checkpoint_first", "<red>You must reach the next checkpoint first!");
                return;
            }

            for (int skippedIndex = expectedIndex; skippedIndex < i; skippedIndex++) {
                ParkourNode skippedNode = nodes.get(skippedIndex);
                parkourTry.reachedNodes().add(ReachedNode.create(skippedNode.index(), i, true));
            }

            handlePointReached(player, parkourTry, node, i);
        }
    }

    private @Nullable ParkourNode getNodeAtPosition(@NotNull Player player, @NotNull Parkour parkour) {
        for (ParkourNode node : parkour.nodes()) {

            if (PlayerUtils.isInsideBox(player, node.pos(), node.boundingBox())) {
                return node;
            }
        }

        return null;
    }

    public void checkFinish(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = getParkour(parkourTry.parkourId());
        if (parkour == null) return;

        if (!PlayerUtils.isInsideBox(player, parkour.endPos(), parkour.endBox())) return;

        if (!hasReachedAllCheckpoints(player)) {
            msgManager.showMessage(player, "parkour_must_reach_all_checkpoints", "<red>You must reach all checkpoints before finishing!");
            return;
        }

        parkourTry.finish();
        handleCleanup(player, parkour, parkourTry);
    }

    public void useReturnToStart(@NotNull Player player) {
        ParkourReturnHolder returnHolder = returnHolders.get(player.getUuid());
        if (returnHolder == null) return;

        invalidateReturnToStart(player);

        Parkour parkour = getParkour(returnHolder.parkourId());
        if (parkour == null) return;

        Pos spawn = parkour.spawnPos();
        if (spawn == null) return;

        player.sendMessage(TextUtils.miniText("<green>Returning to parkour start."));
        PlayerUtils.teleport(player, spawn);

        tipManager.completeTip(player, "parkour_sneak_return_to_start");
    }

    public void invalidateReturnToStart(@NotNull Player player) {
        ParkourReturnHolder returnHolder = returnHolders.get(player.getUuid());
        if (returnHolder == null) return;

        returnHolder.expirationTask().cancel();
        returnHolders.remove(player.getUuid());
    }

    private boolean hasReachedAllCheckpoints(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return false;

        Parkour parkour = getParkour(parkourTry.parkourId());
        if (parkour == null) return false;

        for (ParkourNode node : parkour.nodes()) {
            if (node.isCheckpoint() && !parkourTry.isReached(node.index())) {
                return false;
            }
        }

        return true;
    }

    private void handlePointReached(@NotNull Player player, @NotNull ParkourTry parkourTry, @NotNull ParkourNode node, int i) {
        parkourTry.reachedNodes().add(ReachedNode.create(node.index(), i, false));
        boolean isCheckpoint = node.isCheckpoint();

        parkourTry.setReached(i, isCheckpoint);
        if (isCheckpoint) handleCheckpointReached(player, parkourTry.parkourId());

        if (EnvStatics.DEBUG) {
            player.sendActionBar(TextUtils.miniText("<dark_gray>Node <green>%d</green> reached!", i));
        }
    }

    private void handleCheckpointReached(@NotNull Player player, @NotNull String parkourId) {
        player.sendActionBar(TextUtils.miniText("<yellow>Checkpoint reached!"));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1.0f, 0.8f));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        Pos nextCheckpoint = getNextCheckpoint(player);
        if (nextCheckpoint == null) return;

        GlowingBlockHolder newHolder = PacketUtils.replaceWithGlowing(player, nextCheckpoint);
        glowingHolders.put(player.getUuid(), newHolder);

        PacketUtils.updateWaypoint(player, parkourId, nextCheckpoint);
    }

    private void handleCleanup(@NotNull Player player, @NotNull Parkour parkour, @NotNull ParkourTry parkourTry) {
        parkourTryManager.handleParkourTry(player, parkour, parkourTry);

        // Revert player inventory and tags
        if (player instanceof LobbyPlayer lobbyPlayer) {
            lobbyPlayer.switchToMainInventory();
            lobbyPlayer.removeTag(PlayerTags.IN_PARKOUR);
        }

        players.remove(player.getUuid());

        // Revert glowing block and waypoint
        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        PacketUtils.removeWaypoint(player, parkourTry.parkourId());

        // Create replay and send link
        boolean hasFinished = parkourTry.hasFinished();
        UUID replayId = replayManager.stopTracking(player, hasFinished);

        if (hasFinished && replayId != null) {
            Component replay = TextUtils.miniText("<blue>[Watch]")
                    .clickEvent(ClickEvent.runCommand(String.format("/replay parkour %s", replayId)));

            player.sendMessage(TextUtils.combine(
                    TextUtils.miniText("<gray><i>A replay of this attempt has been recorded</i>.</gray>"),
                    Component.space(),
                    replay
            ));
        }

        // Setup return to start + tip
        if (parkour.spawnPos() == null) return;

        returnHolders.put(player.getUuid(), new ParkourReturnHolder(
                parkourTry.parkourId(),
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    returnHolders.remove(player.getUuid());
                }).delay(TaskSchedule.seconds(10)).schedule()
        ));

        tipManager.showTipLater(
                player,
                "parkour_sneak_return_to_start",
                Duration.ofSeconds(4),
                "Press <b><key:key.sneak></b> to quickly return to the parkour starting point. <gray>(6 seconds left)"
        );
    }
}
