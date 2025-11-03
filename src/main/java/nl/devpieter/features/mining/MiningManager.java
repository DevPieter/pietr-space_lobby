package nl.devpieter.features.mining;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.features.mining.models.MiningHolder;
import nl.devpieter.models.BlockChangeHolder;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class MiningManager {

    private static MiningManager INSTANCE;
    private static EventNode<@NotNull PlayerEvent> EVENT_NODE;

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private static final HashMap<UUID, Task> miningTasks = new HashMap<>();
    private static final HashMap<UUID, MiningHolder> miningPlayers = new HashMap<>();

    private MiningManager() {
    }

    public static MiningManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MiningManager();
        return INSTANCE;
    }

    public EventNode<@NotNull PlayerEvent> getEventNode() {
        if (EVENT_NODE != null) return EVENT_NODE;

        EventNode<@NotNull PlayerEvent> mainNode = EventNode.type(
                "mining-manager",
                EventFilter.PLAYER,
                (_, player) -> player.getGameMode() != GameMode.CREATIVE
        );

        mainNode.addListener(PlayerStartDiggingEvent.class, event -> {
            event.setCancelled(true);

            Block block = event.getBlock();
            Player player = event.getPlayer();
            BlockHolder holder = new BlockHolder(event.getInstance(), event.getBlockPosition(), block);

            if (block.handler() instanceof FarmBlockHandler farmHandler) {
                if (farmHandler.canBeHarvested(holder)) farmHandler.replant(holder, player);
                return;
            }

            if (block.compare(Block.COAL_ORE)) {
                startMining(player, holder, 20 * 2);
            }

            if (block.compare(Block.IRON_ORE)) {
                startMining(player, holder, 20 * 3);
            }
        });

        mainNode.addListener(PlayerCancelDiggingEvent.class, event -> {
            stopMining(event.getPlayer());
        });

        mainNode.addListener(PlayerFinishDiggingEvent.class, event -> {
            stopMining(event.getPlayer());
        });

        mainNode.addListener(PlayerBlockBreakEvent.class, event -> {
            event.setCancelled(true);
        });

        mainNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            event.setCancelled(true);
        });

        EVENT_NODE = mainNode;
        return EVENT_NODE;
    }

    private MiningHolder getMiningHolder(UUID uuid) {
        return miningPlayers.get(uuid);
    }

    private void startMining(Player player, BlockHolder blockHolder, long totalBreakTicks) {
        if (miningPlayers.containsKey(player.getUuid())) stopMining(player);

        MiningHolder holder = new MiningHolder(blockHolder, System.currentTimeMillis(), totalBreakTicks);
        miningPlayers.put(player.getUuid(), holder);

        miningTasks.put(player.getUuid(), scheduler.scheduleTask(() -> {
            MiningHolder currentHolder = getMiningHolder(player.getUuid());
            if (currentHolder == null) {
                stopMining(player);
                return;
            }

            long elapsedTime = System.currentTimeMillis() - currentHolder.startTime();
            long totalBreakTime = currentHolder.totalBreakTicks() * 50;

            int stage = (int) ((elapsedTime * 10) / totalBreakTime);

            sendCrackPackets(player, currentHolder.holder(), stage);
            if (elapsedTime < totalBreakTime) return;

            Instance instance = blockHolder.instance();
            Point point = blockHolder.position();

            BlockChangeHolder changeHolder = new BlockChangeHolder(
                    instance,
                    new BlockHolder(
                            instance,
                            point,
                            blockHolder.block()
                    ),
                    new BlockHolder(
                            instance,
                            point,
                            Block.BEDROCK
                    )
            );

            instance.setBlock(point, Block.BEDROCK);
            initRevertTask(changeHolder);

            stopMining(player);
            miningTasks.remove(player.getUuid());
        }, TaskSchedule.immediate(), TaskSchedule.tick(1)));
    }

    private void stopMining(Player player) {
        MiningHolder holder = miningPlayers.remove(player.getUuid());
        if (holder != null) sendCrackPackets(player, holder.holder(), -1);

        Task task = miningTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }

    private void sendCrackPackets(Player player, BlockHolder holder, int stage) {
        if (stage < 0) stage = -1;
        if (stage > 9) stage = 9;

        int entityId = generateEntityId(holder.position());

        player.sendPacketToViewersAndSelf(new BlockBreakAnimationPacket(
                entityId,
                holder.position(),
                (byte) stage
        ));
    }

    private void initRevertTask(BlockChangeHolder holder) {
        scheduler.buildTask(() -> {
            BlockHolder original = holder.original();
            holder.instance().setBlock(original.position(), original.block());
        }).delay(TaskSchedule.seconds(8)).schedule();
    }

    private int generateEntityId(Point point) {
        return (int) (point.x() * 1000000 + point.y() * 1000 + point.z());
    }
}
