package nl.devpieter.managers;

import net.kyori.adventure.text.Component;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
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
import nl.devpieter.models.Parkour;
import nl.devpieter.models.PlayerParkourHolder;
import nl.devpieter.models.PlayerParkourStat;
import nl.devpieter.utils.PacketUtils;
import nl.devpieter.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ParkourManager {

    private static final double CHECK_RADIUS = 0.8;

    private static ParkourManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();

    private final HashMap<UUID, Parkour> parkours = new HashMap<>();
    private final HashMap<UUID, Entity> leaderboardEntities = new HashMap<>();
    private final HashMap<UUID, PlayerParkourHolder> players = new HashMap<>();

    private ParkourManager() {

        Parkour mainWorldParkour = new Parkour(
                UUID.fromString("ebe258c5-e6ae-41fc-80e0-04509d4f3338"),
                worldManager.getMainWorld().getUuid(),
                new Pos(10.5, 65, 11.5, 0, 0),
                new Pos(-12.5, 73, 3.5, -90, 0),
                List.of(
                        new Pos(20.5, 70, 23.5, -45, 0),
                        new Pos(13.5, 72, 32.5, 90, 0),
                        new Pos(2.5, 71, 36.5, 90, 0),
                        new Pos(-18.5, 74, 18.5, 155, 0),
                        new Pos(-22.5, 75, 10.5, 170, 0)
                ),
                new Pos(12.5, 65, 10.5, 130, 10),
                new ArrayList<>(List.of(new PlayerParkourStat(
                                UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"),
                                "Scorpio88PK",
                                0,
                                0,
                                17700
                        ),
                        new PlayerParkourStat(
                                UUID.fromString("26766de1-0f73-4c1a-bd83-ccaca150237b"),
                                "24uur",
                                0,
                                0,
                                17450
                        )
                ))
        );

        Parkour test = new Parkour(
                UUID.fromString("455a34e8-5181-4e82-b760-72e4c50d3c11"),
                worldManager.getMainWorld().getUuid(),
                new Pos(0.5, 64, -0.5, 0, 0),
                new Pos(2.5, 64, -11.5, -90, 0),
                List.of(
                        new Pos(0.5, 64, -3.5, -45, 0),
                        new Pos(3.5, 64, -5.5, 90, 0)
                ),
                new Pos(0.5, 67, 0.5),
                new ArrayList<>()
        );

        parkours.put(mainWorldParkour.uuid(), mainWorldParkour);
        parkours.put(test.uuid(), test);

        init();
    }

    public static ParkourManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourManager();
        return INSTANCE;
    }

    private void init() {
        for (Parkour parkour : parkours.values()) {
            InstanceContainer instance = worldManager.getWorld(parkour.worldUuid());
            if (instance == null) continue;

            instance.setBlock(parkour.start(), Block.HEAVY_WEIGHTED_PRESSURE_PLATE);
            instance.setBlock(parkour.end(), Block.LIGHT_WEIGHTED_PRESSURE_PLATE);

            for (Pos checkpoint : parkour.checkpoints()) {
                instance.setBlock(checkpoint, Block.SPRUCE_PRESSURE_PLATE);
            }

            Entity leaderboardEntity = new Entity(EntityType.TEXT_DISPLAY);
            leaderboardEntity.setInstance(instance, parkour.leaderboardPos());

            Component title = TextUtils.miniText(
                    "<b><yellow>Leaderboard<reset>\n<dark_gray>Coming soon™\n"
            );

            Component stats = TextUtils.empty();

            List<PlayerParkourStat> leaderboard = parkour.leaderboard().stream()
                    .sorted(Comparator.comparingLong(PlayerParkourStat::finishTime)).toList();
            leaderboard = removeDuplicateStats(leaderboard);

            for (int i = 0; i < leaderboard.size(); i++) {
                PlayerParkourStat stat = leaderboard.get(i);
                String time = TextUtils.getFormattedTime(stat.finishTime(), true, true);
                stats = stats.append(TextUtils.miniText(
                        "\n<gray>%d. %s - %s", (i + 1), stat.playerName(), time
                ));

                if (i >= 4) {
                    stats = stats.append(TextUtils.miniText("\n<gray>..."));
                    break;
                }
            }

            Component finalStats = stats;
            leaderboardEntity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
                textDisplayMeta.setText(title.append(finalStats));
                textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
                textDisplayMeta.setHasNoGravity(true);
                textDisplayMeta.setScale(new Vec(.6, .6, .6));
            });

            leaderboardEntities.put(parkour.uuid(), leaderboardEntity);
        }
    }

    private void updateLeaderboard(@NotNull Parkour parkour) {
        Entity leaderboardEntity = leaderboardEntities.get(parkour.uuid());
        if (leaderboardEntity == null) return;

        Component title = TextUtils.miniText(
                "<b><yellow>Leaderboard<reset>\n<dark_gray>Coming soon™\n"
        );

        Component stats = TextUtils.empty();

        List<PlayerParkourStat> leaderboard = parkour.leaderboard().stream()
                .sorted(Comparator.comparingLong(PlayerParkourStat::finishTime)).toList();
        leaderboard = removeDuplicateStats(leaderboard);

        for (int i = 0; i < leaderboard.size(); i++) {
            PlayerParkourStat stat = leaderboard.get(i);
            String time = TextUtils.getFormattedTime(stat.finishTime(), true, true);
            stats = stats.append(TextUtils.miniText(
                    "\n<gray>%d. %s - %s", (i + 1), stat.playerName(), time
            ));

            if (i >= 4) {
                stats = stats.append(TextUtils.miniText("\n<gray>..."));
                break;
            }
        }

        Component finalStats = stats;
        leaderboardEntity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
            textDisplayMeta.setText(title.append(finalStats));
        });
    }

    private List<PlayerParkourStat> removeDuplicateStats(@NotNull List<PlayerParkourStat> stats) {
        Map<UUID, PlayerParkourStat> uniqueStats = new HashMap<>();

        for (PlayerParkourStat stat : stats) {
            if (!uniqueStats.containsKey(stat.playerUuid()) || uniqueStats.get(stat.playerUuid()).finishTime() > stat.finishTime()) {
                uniqueStats.put(stat.playerUuid(), stat);
            }
        }

        return new ArrayList<>(uniqueStats.values());
    }

    public boolean isDoingParkour(@NotNull Player player) {
        return players.containsKey(player.getUuid());
    }

    public Parkour getParkour(@NotNull UUID uuid) {
        return parkours.get(uuid);
    }

    public Parkour getParkour(@NotNull UUID world, @NotNull Pos pos) {
        for (Parkour parkour : parkours.values()) {
            if (!parkour.worldUuid().equals(world)) continue;
            if (parkour.start().distance(pos) <= CHECK_RADIUS) {
                return parkour;
            }
        }

        return null;
    }

    public Pos getCurrentCheckpoint(@NotNull Player player) {
        PlayerParkourHolder holder = players.get(player.getUuid());
        if (holder == null) return null;

        Parkour parkour = parkours.get(holder.parkourUuid());
        if (parkour == null) return null;

        List<Pos> checkpoints = parkour.checkpoints();
        if (holder.checkpointIndex() < 0 || holder.checkpointIndex() >= checkpoints.size()) {
            return parkour.start();
        }

        return checkpoints.get(holder.checkpointIndex());
    }

    public Pos getNextCheckpoint(@NotNull Player player) {
        PlayerParkourHolder holder = players.get(player.getUuid());
        if (holder == null) return null;

        Parkour parkour = parkours.get(holder.parkourUuid());
        if (parkour == null) return null;

        List<Pos> checkpoints = parkour.checkpoints();
        if (holder.checkpointIndex() + 1 < checkpoints.size()) {
            return checkpoints.get(holder.checkpointIndex() + 1);
        }

        return parkour.end();
    }

    public void startParkour(@NotNull Player player, @NotNull UUID parkourUuid) {
        if (players.containsKey(player.getUuid())) return;

        Parkour parkour = parkours.get(parkourUuid);
        if (parkour == null) return;

        player.sendMessage(TextUtils.miniText("<green>Parkour started! Go go go!"));

        Pos nextCheckpoint = getFirstCheckpoint(parkour);
        int entityId = PacketUtils.replaceWithGlowing(player, nextCheckpoint);

        ItemStack cancelItem = ItemStack.of(Material.BARRIER);
        PacketUtils.setSlot(player, 40, cancelItem);

        ItemStack checkpointItem = ItemStack.of(Material.SPRUCE_PRESSURE_PLATE);
        PacketUtils.setSlot(player, 36, checkpointItem);

        players.put(player.getUuid(), new PlayerParkourHolder(parkourUuid, System.currentTimeMillis(), -1, nextCheckpoint, entityId));
    }

    public void cancelParkour(@NotNull Player player) {
        PlayerParkourHolder holder = players.get(player.getUuid());
        if (holder == null) return;

        Parkour parkour = parkours.get(holder.parkourUuid());
        if (parkour == null) return;

        players.remove(player.getUuid());
        player.sendMessage(TextUtils.miniText("<red>Parkour cancelled!"));

        PacketUtils.setSlot(player, 40, ItemStack.of(Material.AIR));
        PacketUtils.setSlot(player, 36, ItemStack.of(Material.AIR));
        PacketUtils.revertToOriginalBlock(player, holder.entityPos(), holder.entityId());
    }

    public void checkCheckpoint(@NotNull Player player, @NotNull Pos pos) {
        PlayerParkourHolder holder = players.get(player.getUuid());
        if (holder == null) return;

        Parkour parkour = parkours.get(holder.parkourUuid());
        if (parkour == null) return;

        List<Pos> checkpoints = parkour.checkpoints();
        for (int i = 0; i < checkpoints.size(); i++) {
            if (!isInsideBlock(player, checkpoints.get(i))) continue;
            if (holder.checkpointIndex() + 1 != i) continue;

            holder.setCheckpointIndex(i);

            long timeMs = System.currentTimeMillis() - holder.startTime();
            String time = TextUtils.getFormattedTime(timeMs, false, true);
            player.sendMessage(TextUtils.miniText("<yellow>Checkpoint %s reached. <i><gray>%s", (i + 1), time));

            PacketUtils.revertToOriginalBlock(player, holder.entityPos(), holder.entityId());

            Pos nextCheckpoint = getNextCheckpoint(player);
            int entityId = PacketUtils.replaceWithGlowing(player, nextCheckpoint);

            holder.setEntityPos(nextCheckpoint);
            holder.setEntityId(entityId);
            return;
        }
    }

    public void checkFinish(@NotNull Player player, @NotNull Pos pos) {
        PlayerParkourHolder holder = players.get(player.getUuid());
        if (holder == null) return;

        Parkour parkour = getParkour(holder.parkourUuid());
        if (parkour == null) return;

        if (!isInsideBlock(player, parkour.end())) return;

        if (holder.checkpointIndex() < parkour.checkpoints().size() - 1) {
            player.sendMessage(TextUtils.miniText("<red>You must reach all checkpoints before finishing!"));
            return;
        }

        players.remove(player.getUuid());

        long timeMs = System.currentTimeMillis() - holder.startTime();
        String time = TextUtils.getFormattedTime(timeMs, true, false);
        player.sendMessage(TextUtils.miniText("<green>Parkour finished in <b>%s</b>!", time));

        parkour.leaderboard().add(new PlayerParkourStat(
                player.getUuid(),
                player.getUsername(),
                holder.startTime(),
                System.currentTimeMillis(),
                timeMs
        ));
        updateLeaderboard(parkour);

        PacketUtils.setSlot(player, 40, ItemStack.of(Material.AIR));
        PacketUtils.setSlot(player, 36, ItemStack.of(Material.AIR));
        PacketUtils.revertToOriginalBlock(player, holder.entityPos(), holder.entityId());
    }

    public Pos getFirstCheckpoint(@NotNull Parkour parkour) {
        return parkour.checkpoints().isEmpty() ? parkour.end() : parkour.checkpoints().getFirst();
    }

    private boolean isInsideBlock(Entity player, Point blockPos) {
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

        double blockMinX = blockPos.blockX();
        double blockMaxX = blockPos.blockX() + 1;
        double blockMinY = blockPos.blockY();
        double blockMaxY = blockPos.blockY() + .3;
        double blockMinZ = blockPos.blockZ();
        double blockMaxZ = blockPos.blockZ() + 1;

        return (playerMinX < blockMaxX && playerMaxX > blockMinX) &&
                (playerMinY < blockMaxY && playerMaxY > blockMinY) &&
                (playerMinZ < blockMaxZ && playerMaxZ > blockMinZ);
    }
}
