package nl.devpieter.managers;

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
import nl.devpieter.utils.PacketUtils;
import nl.devpieter.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ParkourManager {

    private static final double CHECK_RADIUS = 0.8;

    private static ParkourManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();

    private final HashMap<UUID, Parkour> parkours = new HashMap<>();
    private final HashMap<UUID, PlayerParkourHolder> players = new HashMap<>();

    private ParkourManager() {

        Parkour mainWorldParkour = new Parkour(
                UUID.randomUUID(),
                worldManager.getMainWorld().getUuid(),
                new Pos(10.5, 65, 11.5, 0, 0),
                new Pos(-12.5, 73, 3.5, -90, 0),
                List.of(
                        new Pos(20.5, 70, 23.5, -45, 0),
                        new Pos(13.5, 72, 32.5, 90, 0),
                        new Pos(2.5, 71, 36.5, 90, 0),
                        new Pos(-18.5, 74, 18.5, 155, 0),
                        new Pos(-22.5, 75, 10.5, 170, 0)
                )
        );

        parkours.put(mainWorldParkour.uuid(), mainWorldParkour);
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
        }

        Pos textDisplayPos = new Pos(12.5, 67, 10.5, 0, 0);

        Entity textDisplay = new Entity(EntityType.TEXT_DISPLAY);
        textDisplay.setInstance(this.worldManager.getMainWorld(), textDisplayPos);

        TextDisplayMeta meta = (TextDisplayMeta) textDisplay.getEntityMeta();

        meta.setText(TextUtils.miniText(
                "<b><yellow>Leaderboard<reset>" +
                        "\n<gray>Coming soon™" +
                        "\n\n<gray>Scorpio88PK - 20s 497ms"
        ));

        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setAlignment(TextDisplayMeta.Alignment.CENTER);
        meta.setHasNoGravity(true);
        meta.setScale(new Vec(1, 1, 1));
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
            if (checkpoints.get(i).distance(pos) > CHECK_RADIUS) continue;
            if (holder.checkpointIndex() + 1 != i) continue;

            holder.setCheckpointIndex(i);

            long timeMs = System.currentTimeMillis() - holder.startTime();
            String time = TextUtils.getFormattedTime(timeMs, false);
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

        if (parkour.end().distance(pos) > CHECK_RADIUS) return;

        if (holder.checkpointIndex() < parkour.checkpoints().size() - 1) {
            player.sendMessage(TextUtils.miniText("<red>You must reach all checkpoints before finishing!"));
            return;
        }

        players.remove(player.getUuid());

        long timeMs = System.currentTimeMillis() - holder.startTime();
        String time = TextUtils.getFormattedTime(timeMs, true);
        player.sendMessage(TextUtils.miniText("<green>Parkour finished in <b>%s</b>!", time));

        PacketUtils.setSlot(player, 40, ItemStack.of(Material.AIR));
        PacketUtils.setSlot(player, 36, ItemStack.of(Material.AIR));
        PacketUtils.revertToOriginalBlock(player, holder.entityPos(), holder.entityId());
    }

    public Pos getFirstCheckpoint(@NotNull Parkour parkour) {
        return parkour.checkpoints().isEmpty() ? parkour.end() : parkour.checkpoints().getFirst();
    }


}
