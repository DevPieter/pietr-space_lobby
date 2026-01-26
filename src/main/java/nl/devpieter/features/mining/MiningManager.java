package nl.devpieter.features.mining;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSoundType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.features.mining.blockHandlers.MinableBlockHandler;
import nl.devpieter.features.mining.enums.ToolType;
import nl.devpieter.features.mining.miningSequence.IMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.AirMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.BaseOreMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.CobblestoneMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.StoneMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.mithril.CoreMithrilMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.mithril.InnerMithrilMiningSequence;
import nl.devpieter.features.mining.miningSequence.sequences.mithril.OuterMithrilMiningSequence;
import nl.devpieter.features.mining.models.MiningHolder;
import nl.devpieter.features.mining.models.MiningStep;
import nl.devpieter.features.mining.tool.ITool;
import nl.devpieter.features.mining.tool.tools.FastNetheritePickaxe;
import nl.devpieter.features.mining.tool.tools.IronPickaxe;
import nl.devpieter.features.mining.tool.tools.NetheritePickaxe;
import nl.devpieter.features.mining.tool.tools.WoodenPickaxe;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.models.BlockChangeHolder;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.minestom.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MiningManager {

    private static MiningManager INSTANCE;
    private static EventNode<@NotNull PlayerEvent> EVENT_NODE;

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private final HashMap<PointHolder, IMiningSequence> miningSequences = new HashMap<>();
    private final List<ITool> tools = new ArrayList<>();

    private final HashMap<UUID, Task> miningTasks = new HashMap<>();
    private final HashMap<PointHolder, Task> revertTasks = new HashMap<>();

    private final HashMap<UUID, MiningHolder> miningPlayers = new HashMap<>();

    private MiningManager() {
        tools.addAll(List.of(
                new NetheritePickaxe(),
                new FastNetheritePickaxe(),
                new WoodenPickaxe(),
                new IronPickaxe()
        ));

        init();
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

            BlockHolder holder = new BlockHolder(event.getInstance(), event.getBlockPosition(), event.getBlock());
            tryMine(event.getPlayer(), holder);
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

        mainNode.addListener(PlayerDisconnectEvent.class, event -> {
            stopMining(event.getPlayer());
        });

        // Cancel on player attack?

        EVENT_NODE = mainNode;
        return EVENT_NODE;
    }

    private void init() {
        // TODO - Testing only, remove later
        LoadedWorld mainWorld = WorldManager.getInstance().getLoadedWorldById("main");
        if (mainWorld == null) return;

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.INFESTED_STONE).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new StoneMiningSequence());
        });

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.INFESTED_CRACKED_STONE_BRICKS).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new AirMiningSequence());
        });

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.INFESTED_COBBLESTONE).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new CobblestoneMiningSequence());
        });

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.DIAMOND_ORE).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new BaseOreMiningSequence(MiningSteps.DIAMOND_ORE));
//            miningSequences.put(pointHolder, new OuterMithrilMiningSequence());
//            miningSequences.put(pointHolder, new AirMiningSequence());
        });

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.GRAY_WOOL).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new OuterMithrilMiningSequence());
        });

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.PRISMARINE).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new InnerMithrilMiningSequence());
        });

        WorldUtils.findBlocks(mainWorld.instance(), new Vec(0, 64, 0), 100, Block.LIGHT_BLUE_WOOL).forEach(point -> {
            PointHolder pointHolder = new PointHolder(mainWorld.instance(), point);
            miningSequences.put(pointHolder, new CoreMithrilMiningSequence());
        });

        for (Map.Entry<PointHolder, IMiningSequence> entry : miningSequences.entrySet()) {
            PointHolder pointHolder = entry.getKey();
            pointHolder.instance().setBlock(pointHolder.position(), entry.getValue().buildBlockFromCurrentStep());
        }
    }

    private IMiningSequence getSequenceAt(PointHolder point) {
        return miningSequences.get(point);
    }

    private MiningHolder getMiningHolder(UUID uuid) {
        return miningPlayers.get(uuid);
    }

    private void tryMine(Player player, BlockHolder holder) {
        Block block = holder.block();

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

            if (!canMineUsingTool(minableHandler, tool)) {
                player.sendMessage(TextUtils.miniText("<red>Eek! Your tool is not suitable for mining this block."));
                return;
            }

            if (tool.getMiningStrength() < minableHandler.hardness()) {
                player.sendMessage(TextUtils.miniText("<red>Eek! Your tool is too weak to mine this block."));
                return;
            }

            double speedMul = tool.getMiningSpeedMultiplier();
            double strengthMul = Math.max(0, tool.getMiningStrength() - minableHandler.hardness()) * 0.4;
            double totalMultiplier = speedMul + strengthMul;

            int adjustedBreakTicks = (int) Math.ceil(minableHandler.baseBreakTicks() / totalMultiplier);
            System.out.println("SpeedMul: " + speedMul + ", StrengthMul: " + strengthMul + ", TotalMul: " + totalMultiplier + ", AdjustedBreakTicks: " + adjustedBreakTicks);

            startMining(player, holder, adjustedBreakTicks);
        }
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

            IMiningSequence miningSequence = getSequenceAt(new PointHolder(instance, point));
            if (miningSequence == null) {
                stopMining(player);
                miningTasks.remove(player.getUuid());
                return;
            }

            Block newBlock = miningSequence.increment() != null
                    ? miningSequence.buildBlockFromCurrentStep()
                    : Block.BEDROCK;

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
                            newBlock
                    )
            );

            instance.setBlock(point, newBlock);
            initRevertTask(miningSequence, changeHolder);

            BlockSoundType soundType = blockHolder.block().registry().getBlockSoundType();
            if (soundType != null) SoundUtils.playSoundInWorld(
                    instance,
                    point.add(0.5, 0.5, 0.5),
                    soundType.breakSound(),
                    soundType.volume(), soundType.pitch()
            );

            // TODO - Award XP, drops, etc.

            stopMining(player);
            if (miningSequence.isAtEnd()) return;

            tryMine(player, changeHolder.replacement());
        }, TaskSchedule.immediate(), TaskSchedule.tick(1)));
    }

    private void stopMining(Player player) {
        MiningHolder holder = miningPlayers.remove(player.getUuid());
        if (holder != null) PacketUtils.sendCrackPackets(player, holder.holder(), -1);

        Task task = miningTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }

    private void initRevertTask(IMiningSequence miningSequence, BlockChangeHolder holder) {
        PointHolder point = holder.replacement().asPointHolder();

        Task existingTask = revertTasks.get(point);
        if (existingTask != null) existingTask.cancel();

        MiningStep previousStep = miningSequence.getPreviousStep();
        int revertTicks = miningSequence.getRevertTicks() + (previousStep != null ? previousStep.extraRevertTicks() : 0);

        Task task = scheduler.buildTask(() -> {
            boolean canReplace = point.instance().getPlayers().stream()
                    .noneMatch(player -> PlayerUtils.isInsideBox(player, point.position(), new BoundingBox(
                            new Vec(0, 0, 0),
                            new Vec(1, 1, 1)
                    )));

            if (!canReplace) {
                initRevertTask(miningSequence, holder);
                return;
            }

            MiningStep step = miningSequence.decrement();
            if (step == null) {
                revertTasks.remove(point);
                return;
            }

            Block newBlock = miningSequence.buildBlockFromCurrentStep();
            holder.instance().setBlock(holder.original().position(), newBlock);

            if (miningSequence.isAtBeginning()) revertTasks.remove(point);
            else initRevertTask(miningSequence, holder);

        }).delay(TaskSchedule.tick(revertTicks)).schedule();

        revertTasks.put(point, task);
    }

    private boolean canMineUsingTool(MinableBlockHandler handler, ITool tool) {
        List<ToolType> handlerToolTypes = handler.toolTypes();

        if (handlerToolTypes.contains(ToolType.NONE)) return false;
        if (handlerToolTypes.contains(ToolType.ANY)) return true;
        if (handlerToolTypes.contains(ToolType.HAND)) return true;

        for (ToolType toolType : handlerToolTypes) {
            if (tool.getToolTypes().contains(toolType)) return true;
        }

        return false;
    }
}
