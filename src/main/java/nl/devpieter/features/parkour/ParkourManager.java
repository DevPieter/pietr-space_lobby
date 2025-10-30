package nl.devpieter.features.parkour;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.LobbyPlayer;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.parkour.models.parkour.Parkour;
import nl.devpieter.features.parkour.models.parkour.ParkourNode;
import nl.devpieter.features.parkour.models.parkourTry.FailedNode;
import nl.devpieter.features.parkour.models.parkourTry.ParkourTry;
import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import nl.devpieter.models.GlowingBlockHolder;
import nl.devpieter.models.SlotHolder;
import nl.devpieter.utils.minestom.PacketUtils;
import nl.devpieter.utils.minestom.PlayerUtils;
import nl.devpieter.utils.minestom.TextUtils;
import nl.devpieter.utils.minestom.WorldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ParkourManager {

    private static ParkourManager INSTANCE;

    private final HashMap<String, Parkour> parkours = new HashMap<>();
    private final HashMap<String, List<Parkour>> worldParkourLookup = new HashMap<>();

    private final HashMap<UUID, ParkourTry> players = new HashMap<>();
    private final HashMap<UUID, GlowingBlockHolder> glowingHolders = new HashMap<>();

    private ParkourManager() {
    }

    public static ParkourManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourManager();
        return INSTANCE;
    }

    public void initParkour(@NotNull InstanceContainer instance, @NotNull Parkour parkour) {

        Pos start = parkour.startPos();
        instance.setBlock(start, Block.HEAVY_WEIGHTED_PRESSURE_PLATE);

        Pos end = parkour.endPos();
        instance.setBlock(end, Block.LIGHT_WEIGHTED_PRESSURE_PLATE);

        // DEBUG - start
        Pos debugStart = start.withYaw(0).withPitch(0);
        WorldUtils.placeMarker(instance, debugStart.add(0.5, 0.5, 0.5), TextUtils.miniText("<b><green>Start</green>"));
        WorldUtils.placeBoundingBox(instance, debugStart, parkour.startBox());

        Pos debugEnd = end.withYaw(0).withPitch(0);
        WorldUtils.placeMarker(instance, debugEnd.add(0.5, 0.5, 0.5), TextUtils.miniText("<b><red>End</red>"));
        WorldUtils.placeBoundingBox(instance, debugEnd, parkour.endBox());
        // DEBUG - end

        for (ParkourNode node : parkour.nodes()) {
            Pos pos = node.pos();
            if (node.isCheckpoint()) instance.setBlock(pos, Block.SPRUCE_PRESSURE_PLATE);

            // DEBUG - start
            Pos debugPos = pos.withYaw(0).withPitch(0);
            if (!node.isCheckpoint()) instance.setBlock(debugPos, Block.STONE_PRESSURE_PLATE);

            Component text = TextUtils.miniText("<b><yellow>%s</yellow>", node.index());
            WorldUtils.placeMarker(instance, debugPos.add(0.5, 0.5, 0.5), text);
            WorldUtils.placeBoundingBox(instance, debugPos, node.boundingBox());
            // DEBUG - end
        }

//        parkours.put(parkour.id(), parkour);
//        worldParkourLookup.computeIfAbsent(holder.world().id(), k -> new ArrayList<>()).add(parkour);
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
        glowingHolders.put(player.getUuid(), PacketUtils.replaceWithGlowing(player, nextCheckpoint.add(0.5, 0, 0.5)));
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
            PlayerUtils.teleport(player, parkour.startPos().add(0.5, 0, 0.5));
        } else {
            parkourTry.rollbackToLastCheckpoint();
            PlayerUtils.teleport(player, currentCheckpointNode.pos().add(0.5, 0, 0.5));
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

        player.sendActionBar(TextUtils.miniText("<dark_gray>Node <green>%d</green> reached!", i));
    }

    private void handleCheckpointReached(@NotNull Player player, @NotNull String parkourId) {
        player.sendMessage(TextUtils.miniText("<yellow>Checkpoint reached!"));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1.0f, 0.8f));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        Pos nextCheckpoint = getNextCheckpoint(player);
        if (nextCheckpoint == null) return;

        GlowingBlockHolder newHolder = PacketUtils.replaceWithGlowing(player, nextCheckpoint.add(0.5, 0, 0.5));
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
    }
}
