package nl.devpieter.managers;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.Settings;
import nl.devpieter.enums.ParkourCancelReason;
import nl.devpieter.enums.ParkourFailReason;
import nl.devpieter.models.GlowingBlockHolder;
import nl.devpieter.models.parkour.*;
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
    private final ParkourTryManager parkourTryManager = ParkourTryManager.getInstance();

    private final HashMap<UUID, Parkour> parkours = new HashMap<>();
    private final HashMap<UUID, HashMap<Pos, Parkour>> worldParkourLookup = new HashMap<>();

    private final HashMap<UUID, ParkourTry> players = new HashMap<>();
    private final HashMap<UUID, GlowingBlockHolder> glowingHolders = new HashMap<>();

    private ParkourManager() {
        Parkour mainWorldParkour = new Parkour(
                UUID.fromString("0198b79d-7424-76e9-8aec-d329e60d4814"),
                UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"),

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
                                UUID.fromString("5ac1323a-c581-48e3-949b-2dcfcf242f09"),
                                12,
                                new Pos(9.5, 72, 32.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("46589a43-f992-4ff5-9f8b-506eb853981e"),
                                13,
                                new Pos(4.5, 72, 32.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("fcab0eaa-83da-49cc-b3c7-58bb9cbf90fa"),
                                14,
                                new Pos(2.5, 71, 36.5, 90, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("f308ee47-0450-49d8-aa00-904e17516d75"),
                                15,
                                new Pos(-1.5, 72, 36.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("be0f7998-35da-4daf-ae77-36bf231d1d15"),
                                16,
                                new Pos(-4.5, 72, 33.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("c71b3cc3-65dd-494e-b2f6-a45d1e23b2f0"),
                                17,
                                new Pos(-4.5, 73, 29.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("3605e5b6-bbce-44bf-9a31-8c0cf96a0134"),
                                18,
                                new Pos(-7.5, 73, 26.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("43c8038e-3da2-4516-a776-39cd390ea7f2"),
                                19,
                                new Pos(-11.5, 73, 26.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("e253956c-e59b-4ef4-8e87-36e4d253cf35"),
                                20,
                                new Pos(-12.5, 73, 22.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("47128235-2d68-498c-ac1a-41beb1320318"),
                                21,
                                new Pos(-16.5, 74, 22.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("6e9e9e5b-8fe4-4418-9b2a-a639986e091c"),
                                22,
                                new Pos(-18.5, 74, 18.5, 155, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("6ddb894d-eda1-45e8-b5c7-956758c97869"),
                                23,
                                new Pos(-20.5, 74, 14.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("36ca5385-972a-4558-9c08-029e420836d6"),
                                24,
                                new Pos(-24.5, 75, 14.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("864db554-931a-4161-80ac-bf9b2eff8715"),
                                25,
                                new Pos(-22.5, 75, 10.5, 170, 0),
                                true
                        ),
                        new ParkourNode(
                                UUID.fromString("95c94b5d-25ec-44f2-9fa5-c0b5074c7de6"),
                                26,
                                new Pos(-23.5, 75, 5.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("9f3ef9cd-d42e-40b4-806d-b0fc5c9532f3"),
                                27,
                                new Pos(-21.5, 75, 2.5),
                                false
                        ),
                        new ParkourNode(
                                UUID.fromString("b7e3d1d7-b5af-4f02-99b1-a910df9b0878"),
                                28,
                                new Pos(-17.5, 74, 3.5),
                                false
                        )
                )
        );

        parkours.put(mainWorldParkour.id(), mainWorldParkour);
        worldParkourLookup.computeIfAbsent(UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"), k -> new HashMap<>()).put(mainWorldParkour.start(), mainWorldParkour);
    }

    public static ParkourManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourManager();
        return INSTANCE;
    }

    public void init() {
        for (Parkour parkour : parkours.values()) {
            InstanceContainer instance = worldManager.getWorld(parkour.worldId());
            if (instance == null) continue;

            instance.setBlock(parkour.start(), Block.HEAVY_WEIGHTED_PRESSURE_PLATE);
            instance.setBlock(parkour.end(), Block.LIGHT_WEIGHTED_PRESSURE_PLATE);

            for (ParkourNode node : parkour.nodes()) {
                if (node.isCheckpoint()) instance.setBlock(node.position(), Block.SPRUCE_PRESSURE_PLATE);

                if (Settings.DEBUG) {
                    if (!node.isCheckpoint()) instance.setBlock(node.position(), Block.STONE_PRESSURE_PLATE);

                    Entity leaderboardEntity = new Entity(EntityType.TEXT_DISPLAY);
                    leaderboardEntity.setInstance(instance, node.position().add(0, 0.5, 0));

                    leaderboardEntity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
                        textDisplayMeta.setText(TextUtils.miniText("<b><yellow>%s</yellow>", node.index()));
                        textDisplayMeta.setScale(new Vec(.5, .5, .5));

                        textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                        textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
                        textDisplayMeta.setHasNoGravity(true);
                    });
                }
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
        player.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1.0f, 1.0f));

        Pos nextCheckpoint = getFirstCheckpoint(parkour);
        glowingHolders.put(player.getUuid(), PacketUtils.replaceWithGlowing(player, nextCheckpoint));

        PacketUtils.setWaypoint(player, parkourUuid, nextCheckpoint);

        ItemStack cancelItem = ItemStack.of(Material.BARRIER);
        PacketUtils.setSlot(player, 40, cancelItem);

        ItemStack checkpointItem = ItemStack.of(Material.SPRUCE_PRESSURE_PLATE);
        PacketUtils.setSlot(player, 36, checkpointItem);

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
                parkourTry.failedNodes().add(FailedNode.create(lastReachedNode.id(), ParkourFailReason.Cancelled));
            }
        } else {
            player.sendMessage(TextUtils.miniText("<red>Parkour cancelled, please don't do that!"));
        }

        parkourTry.cancel(reason);
        handleCleanup(player, parkourTry);
    }

    public void returnToCheckpoint(@NotNull Player player, ParkourFailReason reason) {
        ParkourTry parkourTry = players.get(player.getUuid());
        if (parkourTry == null) return;

        Parkour parkour = parkours.get(parkourTry.parkourId());
        if (parkour == null) return;

        int lastReachedIndex = parkourTry.lastReachedNode();
        ParkourNode currentCheckpointNode = getCurrentCheckpointNode(player);

        if (currentCheckpointNode == null) {
            parkourTry.resetProgress();
            player.teleport(parkour.start());
        } else {
            parkourTry.rollbackToCheckpoint(currentCheckpointNode.index());
            player.teleport(currentCheckpointNode.position());
        }

        player.sendMessage(TextUtils.miniText("<red>You failed the jump!"));
        player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1.0f, 1.0f));

        if (lastReachedIndex < 0 || lastReachedIndex >= parkour.nodes().size()) return;

        ParkourNode lastReachedNode = parkour.nodes().get(lastReachedIndex);
        parkourTry.failedNodes().add(FailedNode.create(lastReachedNode.id(), reason));
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
                parkourTry.reachedNodes().add(new ReachedNode(skippedNode.id(), i, System.currentTimeMillis(), true));
            }

            handlePointReached(player, parkourTry, node, i);
        }
    }

    private ParkourNode getNodeAtPosition(@NotNull Player player, @NotNull Parkour parkour) {
        for (ParkourNode node : parkour.nodes()) {
            if (PlayerUtils.isInsideBlock(player, node.position(), 0.3)) {
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

        if (!PlayerUtils.isInsideBlock(player, parkour.end(), 0.3)) return;

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
        parkourTry.reachedNodes().add(new ReachedNode(node.id(), i, System.currentTimeMillis(), false));
        boolean isCheckpoint = node.isCheckpoint();

        parkourTry.setReached(i, isCheckpoint);
        if (isCheckpoint) handleCheckpointReached(player, parkourTry.parkourId());

        if (Settings.DEBUG) {
            player.sendActionBar(TextUtils.miniText("<dark_gray>Node <green>%d</green> reached!", i));
        }
    }

    private void handleCheckpointReached(@NotNull Player player, @NotNull UUID parkourId) {
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
        parkourTryManager.queueParkourTry(parkourTry);
        players.remove(player.getUuid());

        PacketUtils.setSlot(player, 40, ItemStack.of(Material.AIR));
        PacketUtils.setSlot(player, 36, ItemStack.of(Material.AIR));

        GlowingBlockHolder holder = glowingHolders.remove(player.getUuid());
        if (holder != null) PacketUtils.revertToOriginalBlock(player, holder);

        PacketUtils.removeWaypoint(player, parkourTry.parkourId());
    }
}
