package nl.devpieter.features.mining;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.features.mining.blockHandlers.MinableBlockHandler;
import nl.devpieter.features.mining.models.MiningHolder;
import nl.devpieter.features.mining.tool.ITool;
import nl.devpieter.features.mining.tools.FastNetheritePickaxe;
import nl.devpieter.features.mining.tools.IronPickaxe;
import nl.devpieter.features.mining.tools.NetheritePickaxe;
import nl.devpieter.features.mining.tools.WoodenPickaxe;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.models.BlockChangeHolder;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.utils.minestom.PacketUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MiningManager {

    private static MiningManager INSTANCE;
    private static EventNode<@NotNull PlayerEvent> EVENT_NODE;

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private final List<ITool> tools = new ArrayList<>();

    private static final HashMap<UUID, Task> miningTasks = new HashMap<>();
    private static final HashMap<UUID, MiningHolder> miningPlayers = new HashMap<>();

    private MiningManager() {
        tools.addAll(List.of(
                new NetheritePickaxe(),
                new FastNetheritePickaxe(),
                new WoodenPickaxe(),
                new IronPickaxe()
        ));

        // TODO - Testing only, remove later
        LoadedWorld mainWorld = WorldManager.getInstance().getLoadedWorldById("main");
        if (mainWorld == null) return;

        Instance instance = mainWorld.instance();
        instance.setBlock(-6, 66, 1, Block.COAL_ORE.withHandler(new MinableBlockHandler(1, 20 * 2)));
        instance.setBlock(-6, 66, 0, Block.COAL_ORE.withHandler(new MinableBlockHandler(1, 20 * 2)));
        instance.setBlock(-6, 66, -1, Block.COAL_ORE.withHandler(new MinableBlockHandler(1, 20 * 2)));
        instance.setBlock(-6, 67, 1, Block.IRON_BLOCK.withHandler(new MinableBlockHandler(2, 20 * 4)));
        instance.setBlock(-6, 67, 0, Block.IRON_BLOCK.withHandler(new MinableBlockHandler(2, 20 * 4)));
        instance.setBlock(-6, 67, -1, Block.IRON_BLOCK.withHandler(new MinableBlockHandler(2, 20 * 4)));

        instance.setBlock(-2, 66, -2, Block.DIAMOND_BLOCK.withHandler(new MinableBlockHandler(4, 20 * 8)));
        instance.setBlock(-1, 66, -2, Block.DIAMOND_BLOCK.withHandler(new MinableBlockHandler(4, 20 * 8)));
        instance.setBlock(0, 66, -2, Block.DIAMOND_BLOCK.withHandler(new MinableBlockHandler(4, 20 * 8)));
    }

    public static MiningManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MiningManager();
        return INSTANCE;
    }

    public List<ITool> getTools() {
        return tools;
    }

    public @Nullable ITool getToolByItem(ItemStack itemStack) {
        for (ITool tool : this.tools) {
            UUID itemId = itemStack.getTag(Tag.UUID("tool_id"));
            if (itemId == null) continue;

            if (tool.getId().equals(itemId)) return tool;
        }

        return null;
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

            if (block.handler() instanceof MinableBlockHandler minableHandler) {
                ItemStack itemInHand = player.getItemInHand(PlayerHand.MAIN);

                ITool tool = getToolByItem(itemInHand);
                if (tool == null) {
                    player.sendMessage(TextUtils.miniText("<red>Eek! You need a proper tool to mine this block."));
                    return;
                }

                if (tool.getMiningStrength() < minableHandler.hardness()) {
                    player.sendMessage(TextUtils.miniText("<red>Eek! Your tool is too weak to mine this block."));
                    return;
                }

                double speedMul = tool.getMiningSpeedMultiplier();
                double strengthMul = Math.max(0, tool.getMiningStrength() - minableHandler.hardness()) * 0.3;
                double totalMultiplier = speedMul + strengthMul;

                int adjustedBreakTicks = (int) Math.ceil(minableHandler.totalBreakTicks() / totalMultiplier);
                System.out.println("SpeedMul: " + speedMul + ", StrengthMul: " + strengthMul + ", TotalMul: " + totalMultiplier + ", AdjustedBreakTicks: " + adjustedBreakTicks);

                startMining(player, holder, adjustedBreakTicks);
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

    private void startMining(Player player, BlockHolder blockHolder, int totalBreakTicks) {
        if (miningPlayers.containsKey(player.getUuid())) stopMining(player);

        MiningHolder holder = new MiningHolder(blockHolder, System.currentTimeMillis(), totalBreakTicks);
        miningPlayers.put(player.getUuid(), holder);

        miningTasks.put(player.getUuid(), scheduler.scheduleTask(() -> {
            MiningHolder currentHolder = getMiningHolder(player.getUuid());
            if (currentHolder == null) {
                stopMining(player);
                return;
            }

            int stage = currentHolder.stage();
            if (stage == currentHolder.previousStage()) return;

            currentHolder.updatePreviousStage();
            PacketUtils.sendCrackPackets(player, currentHolder.holder(), stage);

            if (!currentHolder.isComplete()) return;

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
        if (holder != null) PacketUtils.sendCrackPackets(player, holder.holder(), -1);

        Task task = miningTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }

    private void initRevertTask(BlockChangeHolder holder) {
        scheduler.buildTask(() -> {
            BlockHolder original = holder.original();
            holder.instance().setBlock(original.position(), original.block());
        }).delay(TaskSchedule.seconds(8)).schedule();
    }
}
