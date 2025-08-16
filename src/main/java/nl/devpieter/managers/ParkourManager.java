package nl.devpieter.managers;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.models.*;
import nl.devpieter.utils.PacketUtils;
import nl.devpieter.utils.PlayerUtils;
import nl.devpieter.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParkourManager {

    private static ParkourManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();

    private final HashMap<UUID, Parkour> parkours = new HashMap<>();
    private final HashMap<UUID, HashMap<Pos, Parkour>> worldParkourLookup = new HashMap<>();

    private final HashMap<UUID, ParkourTry> players = new HashMap<>();
    private final HashMap<UUID, GlowingBlockHolder> glowingHolders = new HashMap<>();

    private ParkourManager() {
        UUID worldId = worldManager.getMainWorld().getUuid();

        Parkour mainWorldParkour = new Parkour(
                UUID.fromString("ebe258c5-e6ae-41fc-80e0-04509d4f3338"),
                worldId,

                "Main World Parkour",
                new Pos(10.5, 65, 11.5, 0, 0),
                new Pos(-12.5, 73, 3.5, -90, 0),

                new Pos(12.5, 65, 10.5, 130, 10),

                List.of(
                        new ParkourNode(
                                UUID.fromString("c0da787a-9ec0-4a36-8e5a-198e23a38fd2"),
                                0,
                                new Pos(10.5, 66, 14.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("596d5909-eac9-4b8d-88a7-70d399aab2a4"),
                                1,
                                new Pos(13.5, 67, 17.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("01f89836-d1c9-46e4-b43d-34b48079960e"),
                                2,
                                new Pos(16.5, 68, 17.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("e1207709-e6fd-4401-a25c-c6b871426a4c"),
                                3,
                                new Pos(16.5, 69, 20.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("e3abc430-6633-4269-888b-cd192912230a"),
                                4,
                                new Pos(16.5, 70, 23.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("0132b7a6-bb6b-45b4-95a7-e65e3a693bca"),
                                5,
                                new Pos(20.5, 70, 23.5, -45, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("8627164c-91c4-45fd-8ab8-c4cca610e631"),
                                6,
                                new Pos(23.5, 70, 26.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("31f2db12-f681-4987-ac66-5640ab1ce8e0"),
                                7,
                                new Pos(23.5, 71, 29.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("7e33cc92-1946-4a98-a389-c67829d8afc8"),
                                8,
                                new Pos(20.5, 71, 31.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("afc2d540-7a3b-4c95-857c-528dab7aad5f"),
                                9,
                                new Pos(19.5, 72, 34.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("999c18e1-afb3-457b-8f9b-8fe642f70715"),
                                10,
                                new Pos(15.5, 72, 35.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("a268e32f-989f-46cd-9250-50e39751ce67"),
                                11,
                                new Pos(13.5, 72, 32.5, 90, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("fcab0eaa-83da-49cc-b3c7-58bb9cbf90fa"),
                                12,
                                new Pos(2.5, 71, 36.5, 90, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("6e9e9e5b-8fe4-4418-9b2a-a639986e091c"),
                                13,
                                new Pos(-18.5, 74, 18.5, 155, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("864db554-931a-4161-80ac-bf9b2eff8715"),
                                14,
                                new Pos(-22.5, 75, 10.5, 170, 0),
                                true
                        )
                )
        );

        parkours.put(mainWorldParkour.id(), mainWorldParkour);
        worldParkourLookup.computeIfAbsent(worldId, k -> new HashMap<>()).put(mainWorldParkour.start(), mainWorldParkour);
        init();
    }

    public static ParkourManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourManager();
        return INSTANCE;
    }

    private void init() {
        for (Parkour parkour : parkours.values()) {
            InstanceContainer instance = worldManager.getWorld(parkour.worldId());
            if (instance == null) continue;

            instance.setBlock(parkour.start(), Block.HEAVY_WEIGHTED_PRESSURE_PLATE);
            instance.setBlock(parkour.end(), Block.LIGHT_WEIGHTED_PRESSURE_PLATE);

            for (ParkourNode node : parkour.nodes()) {
                if (!node.isCheckpoint()) continue;
                instance.setBlock(node.position(), Block.SPRUCE_PRESSURE_PLATE);
            }
        }
    }

    public boolean isDoingParkour(@NotNull Player player) {
        return players.containsKey(player.getUuid());
    }

    private Parkour getParkour(@NotNull UUID uuid) {
        return parkours.get(uuid);
    }

    public Parkour getParkour(UUID world, Player player) {
        HashMap<Pos, Parkour> starts = worldParkourLookup.get(world);
        if (starts == null) return null;

        for (Map.Entry<Pos, Parkour> entry : starts.entrySet()) {
            if (PlayerUtils.isInsideBlock(player, entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Pos getFirstCheckpoint(@NotNull Parkour parkour) {
        List<ParkourNode> nodes = parkour.nodes();
        for (ParkourNode node : nodes) {
            if (node.isCheckpoint()) return node.position();
        }

        return parkour.end();
    }

    private ParkourNode getCurrentCheckpointNode(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return null;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return null;

        int lastReachedIndex = parkourTry.lastCheckpoint();
        if (lastReachedIndex < 0) return null;

        if (lastReachedIndex >= parkour.nodes().size()) return null;

        ParkourNode lastReachedNode = parkour.nodes().get(lastReachedIndex);
        if (lastReachedNode.isCheckpoint()) return lastReachedNode;

        return null;
    }

    private Pos getCurrentCheckpoint(@NotNull Player player) {
        ParkourNode currentCheckpointNode = getCurrentCheckpointNode(player);
        if (currentCheckpointNode != null) return currentCheckpointNode.position();

        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return null;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return null;

        return parkour.start();
    }

    private Pos getNextCheckpoint(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return null;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return null;

        int lastReachedIndex = parkourTry.lastReachedNode();
        if (lastReachedIndex < 0) {
            return getFirstCheckpoint(parkour);
        }

        if (lastReachedIndex >= parkour.nodes().size()) {
            return parkour.end();
        }

        for (int i = lastReachedIndex + 1; i < parkour.nodes().size(); i++) {
            ParkourNode node = parkour.nodes().get(i);
            if (node.isCheckpoint()) return node.position();
        }

        return parkour.end();
    }

    public void startParkour(@NotNull Player player, @NotNull UUID parkourUuid) {
        if (players.containsKey(player.getUuid())) return;

        Parkour parkour = parkours.get(parkourUuid);
        if (parkour == null) return;

        player.sendMessage(TextUtils.miniText("<green>Parkour started! Go go go!"));

        Pos nextCheckpoint = getFirstCheckpoint(parkour);
        glowingHolders.put(player.getUuid(), PacketUtils.replaceWithGlowing(player, nextCheckpoint));

        ItemStack cancelItem = ItemStack.of(Material.BARRIER);
        PacketUtils.setSlot(player, 40, cancelItem);

        ItemStack checkpointItem = ItemStack.of(Material.SPRUCE_PRESSURE_PLATE);
        PacketUtils.setSlot(player, 36, checkpointItem);

        players.put(player.getUuid(), new ParkourTry(
                parkourUuid,
                player.getUuid()
        ));
    }

    public void cancelParkour(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        parkourTry.setEndTime(System.currentTimeMillis());
        // TODO: queue the parkour try to be saved to the API

        players.remove(player.getUuid());
        player.sendMessage(TextUtils.miniText("<red>Parkour cancelled!"));

        PacketUtils.setSlot(player, 40, ItemStack.of(Material.AIR));
        PacketUtils.setSlot(player, 36, ItemStack.of(Material.AIR));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);
    }

    public void returnToCheckpoint(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        Pos checkpoint = getCurrentCheckpoint(player);
        if (checkpoint == null) return;

//        int lastReachedIndex = parkourTry.nodes().isEmpty() ? -1 : parkourTry.nodes().getLast().reachedAtIndex();
        int lastReachedIndex = parkourTry.lastReachedNode();
        ParkourNode lastParkourNode = parkour.nodes().get(lastReachedIndex);

        parkourTry.failedNodes().add(new FailedNode(
                lastParkourNode.id(),
                System.currentTimeMillis()
        ));

        ParkourNode currentCheckpointNode = getCurrentCheckpointNode(player);
        if (currentCheckpointNode != null) {
            int currentCheckpointIndex = currentCheckpointNode.index();

            for (int i = parkourTry.nodes().size() - 1; i >= 0; i--) {
                ReachedNode node = parkourTry.nodes().get(i);

                if (node.reachedAtIndex() > currentCheckpointIndex) {
                    parkourTry.setReached(node.reachedAtIndex(), false);
                    parkourTry.nodes().remove(i);
                }
            }

            parkourTry.setLastReachedNode(currentCheckpointIndex);
            parkourTry.setLastCheckpoint(currentCheckpointIndex);
        } else {
            parkourTry.setLastReachedNode(-1);
            parkourTry.setLastCheckpoint(-1);

            for (ParkourNode node : parkour.nodes()) {
                parkourTry.setReached(node.index(), false);
            }

            parkourTry.nodes().clear();
        }

        // TODO - This is for debugging purposes, remove later
        int failCountTotal = parkourTry.failedNodes().size();
        int failCountCurrent = (int) parkourTry.failedNodes().stream().filter(node -> node.nodeId().equals(lastParkourNode.id())).count();
        player.sendMessage(TextUtils.miniText("<hover:show_text:'<red>Total: %d\nCurrent: %d'><red>You failed the jump!",
                failCountTotal, failCountCurrent
        ));

        player.teleport(checkpoint);
    }

    public void checkNode(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        List<ParkourNode> nodes = parkour.nodes();
        int expectedIndex = parkourTry.lastReachedNode() + 1;

        BlockVec pos = new BlockVec(player.getPosition());
        ParkourNode node = parkour.nodeMap().get(pos);
        if (node == null) return;

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
                parkourTry.nodes().add(new ReachedNode(skippedNode.id(), i, System.currentTimeMillis(), true));
            }

            handlePointReached(player, parkourTry, node, i);
        }
    }

    public void checkFinish(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = getParkour(parkourTry.parkourId());
        if (parkour == null) return;

        if (!PlayerUtils.isInsideBlock(player, parkour.end(), 0.3)) return;

        if (!hasReachedAllCheckpoints(player)) {
            player.sendMessage(TextUtils.miniText("<red>You must reach all checkpoints before finishing!"));
            return;
        }

        players.remove(player.getUuid());

        long timeMs = System.currentTimeMillis() - parkourTry.startTime();
        String time = TextUtils.getFormattedTime(timeMs, true, false);

        // TODO - This is for debugging purposes, remove later
        int nodesSkipped = 0;
        for (int i = 0; i < parkourTry.nodes().size(); i++) {
            if (parkourTry.nodes().get(i).skipped()) {
                nodesSkipped++;
            }
        }

        player.sendMessage(TextUtils.miniText("<green>Parkour finished in <b>%s</b> with <b>%d</b> skipped!", time, nodesSkipped));

        PacketUtils.setSlot(player, 40, ItemStack.of(Material.AIR));
        PacketUtils.setSlot(player, 36, ItemStack.of(Material.AIR));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        // TODO: queue the parkour try to be saved to the API
    }

    private boolean hasReachedAllCheckpoints(@NotNull Player player) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return false;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return false;

        List<ReachedNode> reachedNodes = parkourTry.nodes();

        for (ParkourNode node : parkour.nodes()) {
            if (!node.isCheckpoint()) continue;

            boolean reached = reachedNodes.stream().anyMatch(r -> r.reachedAtIndex() == node.index());
            if (!reached) return false;
        }

        return true;
    }

    private void handlePointReached(@NotNull Player player, @NotNull ParkourTry parkourTry, @NotNull ParkourNode node, int i) {
        parkourTry.nodes().add(new ReachedNode(node.id(), i, System.currentTimeMillis(), false));

        parkourTry.setReached(i, true);
        parkourTry.setLastReachedNode(i);

        if (node.isCheckpoint()) {
            parkourTry.setLastCheckpoint(i);
            handleCheckpointReached(player);
        } else {
            player.sendMessage(TextUtils.miniText("<green>Node reached!"));
        }
    }

    private void handleCheckpointReached(@NotNull Player player) {
        player.sendMessage(TextUtils.miniText("<yellow>Checkpoint reached!"));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        Pos nextCheckpoint = getNextCheckpoint(player);
        if (nextCheckpoint == null) return;

        GlowingBlockHolder newHolder = PacketUtils.replaceWithGlowing(player, nextCheckpoint);
        glowingHolders.put(player.getUuid(), newHolder);
    }
}
