package nl.devpieter.features.parkour;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.LobbyPlayer;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.parkour.decodables.*;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.parkour.models.parkour.*;
import nl.devpieter.features.parkour.models.parkourTry.FailedNode;
import nl.devpieter.features.parkour.models.parkourTry.ParkourTry;
import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import nl.devpieter.features.signs.SignDecoder;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.models.SignHolder;
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

import java.util.*;

public class ParkourManager {

    private static ParkourManager INSTANCE;
    private static final String SIGN_KEY = "<PARKOUR>";

    private final ComponentLogger logger = MinecraftServer.LOGGER;
    private final SignManager signManager = SignManager.getInstance();
    private final WorldManager worldManager = WorldManager.getInstance();

    private final List<Entity> debugMarkers = new ArrayList<>();

    private final HashMap<String, Parkour> parkours = new HashMap<>();
    private final HashMap<String, List<Parkour>> worldParkourLookup = new HashMap<>();

    private final HashMap<UUID, ParkourTry> players = new HashMap<>();
    private final HashMap<UUID, GlowingBlockHolder> glowingHolders = new HashMap<>();

    private final HashMap<UUID, ParkourReturnHolder> returnHolders = new HashMap<>();

    record ParkourReturnHolder(
            String parkourId,
            Task expirationTask
    ) {

    }

    private ParkourManager() {
    }

    public static ParkourManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourManager();
        return INSTANCE;
    }

    public void init() {
        scanAndRegisterParkours();
    }

    private void scanAndRegisterParkours() {
        HashMap<String, SignHolder> foundParkours = new HashMap<>();
        List<String> incompleteParkours = new ArrayList<>();

        HashMap<String, ParkourSpawnNode> spawnNodes = new HashMap<>();
        HashMap<String, ParkourLeaderboardNode> leaderboardNodes = new HashMap<>();
        HashMap<String, ParkourStartNode> startNodes = new HashMap<>();
        HashMap<String, ParkourEndNode> endNodes = new HashMap<>();

        HashMap<String, List<ParkourNode>> normalNodes = new HashMap<>();
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

            String identifier = String.format("%s:%s", world.id(), name);
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

    public void initParkour(@NotNull LoadedWorld world, @NotNull Parkour parkour) {
        InstanceContainer instance = world.instance();

        Pos start = parkour.startPos();
        instance.setBlock(start, Block.HEAVY_WEIGHTED_PRESSURE_PLATE);

        Pos end = parkour.endPos();
        instance.setBlock(end, Block.LIGHT_WEIGHTED_PRESSURE_PLATE);

        if (EnvStatics.DEBUG) {
            Pos debugStart = start.withYaw(0).withPitch(0);
            WorldUtils.placeMarker(instance, debugStart.add(0, 0.7, 0), TextUtils.miniText("<#cea0d0>%s <gray>Start", parkour.id()));
            WorldUtils.placeBoundingBox(instance, debugStart, parkour.startBox());

            Pos debugEnd = end.withYaw(0).withPitch(0);
            WorldUtils.placeMarker(instance, debugEnd.add(0, 0.7, 0), TextUtils.miniText("<#cea0d0>%s <gray>End", parkour.id()));
            WorldUtils.placeBoundingBox(instance, debugEnd, parkour.endBox());
        }

        for (ParkourNode node : parkour.nodes()) {
            Pos point = node.pos();
            if (node.isCheckpoint()) instance.setBlock(point, Block.SPRUCE_PRESSURE_PLATE);

            if (EnvStatics.DEBUG) {
                Pos debugPos = point.withYaw(0).withPitch(0);
                if (!node.isCheckpoint()) instance.setBlock(debugPos, Block.STONE_PRESSURE_PLATE);

                Component text = TextUtils.miniText("<#cea0d0>%s <gray>%s", parkour.id(), node.index());

                if (node.isCheckpoint()) {
                    text = TextUtils.miniText("<#cea0d0>%s <gray>%s (checkpoint)", parkour.id(), node.index());
                }

                WorldUtils.placeMarker(instance, debugPos.add(0, 0.7, 0), text);
                WorldUtils.placeBoundingBox(instance, debugPos, node.boundingBox());
            }
        }

        parkours.put(parkour.id(), parkour);
        worldParkourLookup.computeIfAbsent(world.id(), _ -> new ArrayList<>()).add(parkour);
    }

    public boolean isDoingParkour(@NotNull Player player) {
        return players.containsKey(player.getUuid());
    }

    private @Nullable Parkour getParkour(@NotNull String uuid) {
        return parkours.get(uuid);
    }

    public @Nullable Parkour getParkour(@NotNull String world, @NotNull Player player) {
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

    public void startParkour(@NotNull Player player, @NotNull String parkourUuid) {
        if (players.containsKey(player.getUuid())) return;

        Parkour parkour = parkours.get(parkourUuid);
        if (parkour == null) return;

        player.sendMessage(TextUtils.miniText("<green>Parkour started! Go go go!"));
        player.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1.0f, 1.0f));

        Pos nextCheckpoint = getFirstCheckpoint(parkour);
        glowingHolders.put(player.getUuid(), PacketUtils.replaceWithGlowing(player, nextCheckpoint));
        PacketUtils.setWaypoint(player, parkourUuid, nextCheckpoint);

        if (player instanceof LobbyPlayer lobbyPlayer) {
            ItemStack cancelItem = ItemStack.of(Material.BARRIER);
            ItemStack checkpointItem = ItemStack.of(Material.SPRUCE_PRESSURE_PLATE);

            lobbyPlayer.fillDynamicInventory(
                    new SlotHolder(0, checkpointItem),
                    new SlotHolder(4, cancelItem)
            );

            lobbyPlayer.switchToDynamicInventory();
        }

        players.put(player.getUuid(), new ParkourTry(
                parkourUuid,
                player.getUuid()
        ));
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
        handleCleanup(player, parkourTry);
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

        player.sendMessage(TextUtils.miniText("<red>You failed the jump!"));
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
                player.sendMessage(TextUtils.miniText("<red>You must reach the next checkpoint first!"));
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
            player.sendMessage(TextUtils.miniText("<red>You must reach all checkpoints before finishing!"));
            return;
        }

        parkourTry.finish();
        handleCleanup(player, parkourTry);

        long timeMs = parkourTry.endTime() - parkourTry.startTime();
        String time = TextUtils.getFormattedTime(timeMs, true, false);

        player.sendMessage(TextUtils.miniText("<green>Parkour finished in <b>%s</b>!", time));
        player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1.0f, 1.0f));
    }

    public void tryReturnToStart(@NotNull Player player) {
        ParkourReturnHolder returnHolder = returnHolders.get(player.getUuid());
        if (returnHolder == null) return;

        Parkour parkour = getParkour(returnHolder.parkourId());
        if (parkour == null) return;

        Pos spawn = parkour.spawnPos();
        if (spawn == null) return;

        player.sendMessage(TextUtils.miniText("<green>Returning to parkour start."));
        PlayerUtils.teleport(player, spawn);

        returnHolder.expirationTask().cancel();
        returnHolders.remove(player.getUuid());

        if (!tipCompleted.contains(player.getUuid())) {
            tipCompleted.add(player.getUuid());
        }
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
        player.sendMessage(TextUtils.miniText("<yellow>Checkpoint reached!"));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1.0f, 0.8f));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        Pos nextCheckpoint = getNextCheckpoint(player);
        if (nextCheckpoint == null) return;

        GlowingBlockHolder newHolder = PacketUtils.replaceWithGlowing(player, nextCheckpoint);
        glowingHolders.put(player.getUuid(), newHolder);

        PacketUtils.updateWaypoint(player, parkourId, nextCheckpoint);
    }

    private void handleCleanup(@NotNull Player player, @NotNull ParkourTry parkourTry) {
//        parkourTryManager.queueParkourTry(parkourTry);
        players.remove(player.getUuid());

        if (player instanceof LobbyPlayer lobbyPlayer) {
            lobbyPlayer.switchToMainInventory();
        }

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        PacketUtils.removeWaypoint(player, parkourTry.parkourId());

        returnHolders.put(player.getUuid(), new ParkourReturnHolder(
                parkourTry.parkourId(),
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    returnHolders.remove(player.getUuid());
                }).delay(TaskSchedule.seconds(3)).schedule()
        ));

        sendTip(player, "<gray>Press <b><key:key.sneak></b> to return to parkour start.");
    }

    // TODO - Move to TipManager or related
    private final HashMap<UUID, Long> tipCooldowns = new HashMap<>();
    private final List<UUID> tipCompleted = new ArrayList<>();

    private void sendTip(@NotNull Player player, @NotNull String message) {
        if (tipCompleted.contains(player.getUuid())) return;

        long currentTime = System.currentTimeMillis();
        Long lastSentTime = tipCooldowns.get(player.getUuid());

        if (lastSentTime != null && (currentTime - lastSentTime) < 10_000) {
            return;
        }

        tipCooldowns.put(player.getUuid(), currentTime);

        player.sendMessage(Component.text()
                .append(TextUtils.miniText("<gray>["))
                .append(TextUtils.miniText("<yellow>Parkour Tip"))
                .append(TextUtils.miniText("<gray>] "))
                .append(TextUtils.miniText(message))
        );
    }
}
