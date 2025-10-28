package nl.devpieter;

import net.kyori.adventure.key.Key;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.blockHandler.DoorBlockHandler;
import nl.devpieter.blockHandler.SignBlockHandler;
import nl.devpieter.commands.ServerCommand;
import nl.devpieter.commands.TestCommand;
import nl.devpieter.commands.player.GamemodeCommand;
import nl.devpieter.commands.world.*;
import nl.devpieter.config.ConfigManager;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.experience.ExperienceManager;
import nl.devpieter.features.farming.listeners.PlayerBlockBreakListener;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.FishingReferenceHolder;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.features.fishing.listeners.PlayerUseItemListener;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.ParkourTryManager;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.parkour.listeners.ParkourPlayerMoveListener;
import nl.devpieter.features.portals.listeners.PortalPlayerMoveListener;
import nl.devpieter.features.sitting.SitManager;
import nl.devpieter.features.sitting.listeners.PlayerBlockInteractListener;
import nl.devpieter.features.sitting.listeners.PlayerStartSneakingListener;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.listeners.PlayerConfigurationListener;
import nl.devpieter.listeners.PlayerDisconnectListener;
import nl.devpieter.listeners.PlayerLoadedListener;
import nl.devpieter.listeners.PlayerMoveListener;
import nl.devpieter.sees.Sees;
import nl.devpieter.utils.minestom.PacketUtils;
import nl.devpieter.utils.minestom.SoundUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) {
        Logger logger = MinecraftServer.LOGGER;
        if (EnvStatics.DEBUG) logger.warn("Debug mode is enabled");

        ConfigManager configManager = ConfigManager.getInstance();
        configManager.load();

        boolean useVelocity = configManager.getBoolean("use_velocity_proxy");
        String velocitySecret = configManager.getString("velocity_proxy_secret");

        Auth auth = useVelocity ? new Auth.Velocity(velocitySecret) : new Auth.Online();
        logger.info("Using authentication: {}", useVelocity ? "Velocity" : "Online");

        MinecraftServer server = MinecraftServer.init(auth);
        MinecraftServer.setBrandName("Pieter Space Lobby");

        MinecraftServer.getBlockManager().registerHandler(Key.key("minecraft:sign"), SignBlockHandler::new);

        // TODO - A bit hacky, but I don't know a better way yet
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
            Block block = event.getBlock();
            Instance instance = event.getInstance();

            if (block.key().asString().endsWith("_door")) {
                if (block.handler() instanceof DoorBlockHandler) return;

                BlockHandler handler = new DoorBlockHandler();
                instance.setBlock(event.getBlockPosition(), block.withHandler(handler));

                handler.onInteract(new BlockHandler.Interaction(
                        block,
                        instance,
                        event.getBlockFace(),
                        event.getBlockPosition(),
                        event.getCursorPosition(),
                        event.getPlayer(),
                        event.getHand()
                ));
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PickupItemEvent.class, event -> {
            FishingManager manager = FishingManager.getInstance();

            FishingReferenceHolder reference = manager.getReference(event.getItemEntity());
            if (reference == null) return;

            Player player = reference.player();
            if (player.getUuid() != event.getEntity().getUuid()) {
                event.setCancelled(true);
                return;
            }

            manager.unregisterReference(event.getEntity());
            reference.catchable().onPickup(player, reference.rod());

            IRod rod = reference.rod();
            ICatchable catchable = reference.catchable();

            player.sendMessage(TextUtils.miniText("<#586373>You caught a ")
                    .append(catchable.getName())
                    .append(TextUtils.miniText("<#586373> with your "))
                    .append(rod.getName())
                    .append(TextUtils.miniText("<#586373>!"))
            );

            boolean isSitting = SitManager.getInstance().isSitting(player);
            boolean isRaining = player.getInstance() != null && player.getInstance().getWeather().isRaining();

            int bonus = isRaining ? 1 : 0;
            bonus += isSitting ? 2 : 0;

            ExperienceManager.getInstance().grantExperience(player, "fishing", 3, bonus);
            playSoundEffect(player);
        });

        Sees sees = Sees.getInstance();
        sees.subscribe(new MainListener());

        WorldManager worldManager = WorldManager.getInstance();
        worldManager.init();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(new PlayerConfigurationListener());
        eventHandler.addListener(new PlayerMoveListener());

        eventHandler.addListener(new PlayerBlockBreakListener());
        eventHandler.addListener(new PlayerBlockInteractListener());
        eventHandler.addListener(new PlayerStartSneakingListener());
        eventHandler.addListener(new PlayerUseItemListener());

        eventHandler.addListener(new PlayerLoadedListener());
        eventHandler.addListener(new PlayerDisconnectListener());

        eventHandler.addListener(new PortalPlayerMoveListener());
        eventHandler.addListener(new ParkourPlayerMoveListener());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            int slot = player.getHeldSlot();

            if (slot == 0) ParkourManager.getInstance().returnToCheckpoint(player, ParkourFailReason.PlayerRequest);
            else if (slot == 4) ParkourManager.getInstance().cancelParkour(player, ParkourCancelReason.PlayerRequest);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, event -> {
            String name = event.getPacket().getClass().getSimpleName();
//            if (name.equals("ClientTickEndPacket")) return;
//            if (name.equals("ClientPlayerPositionPacket")) return;
//            if (name.equals("ClientPlayerPositionAndRotationPacket")) return;
//            if (name.equals("ClientPlayerRotationPacket")) return;
//
//            System.out.println("Received packet: " + event.getPacket().getClass().getSimpleName());
//
//            // ClientClickWindowPacket

            if (
                    name.equals("ClientClickWindowPacket") ||
                            name.equals("ClientEntityActionPacket") ||
                            name.equals("ClientCloseWindowPacket")
            ) {
                if (!ParkourManager.getInstance().isDoingParkour(event.getPlayer())) return;

                ItemStack cancelItem = ItemStack.of(Material.BARRIER);
                PacketUtils.setSlot(event.getPlayer(), 40, cancelItem);

                ItemStack checkpointItem = ItemStack.of(Material.SPRUCE_PRESSURE_PLATE);
                PacketUtils.setSlot(event.getPlayer(), 36, checkpointItem);
                event.setCancelled(true);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);

            ItemStack cancelItem = ItemStack.of(Material.BARRIER);
            PacketUtils.setSlot(event.getPlayer(), 40, cancelItem);

            ItemStack checkpointItem = ItemStack.of(Material.SPRUCE_PRESSURE_PLATE);
            PacketUtils.setSlot(event.getPlayer(), 36, checkpointItem);
        });

        eventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
        });

        eventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
        });

        CommandManager commandManager = MinecraftServer.getCommandManager();

        // Player
        commandManager.register(new GamemodeCommand());

        // World
        commandManager.register(new WorldCommand());
        commandManager.register(new SignCommand());
        commandManager.register(new CreatureCommand());
        commandManager.register(new FarmCommand());
        commandManager.register(new PortalCommand());

        commandManager.register(new TimeCommand());
        commandManager.register(new WeatherCommand());

        // Misc
        commandManager.register(new TestCommand());
        commandManager.register(new ServerCommand());

        ParkourTryManager.getInstance().start();
        server.start("0.0.0.0", 25565);
    }

    private static void playSoundEffect(Player player) {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        AtomicReference<Float> pitch = new AtomicReference<>(0.6f);

        Task task = scheduler.scheduleTask(() -> {
            SoundUtils.playSoundForPlayer(player, SoundEvent.BLOCK_NOTE_BLOCK_BASS, 1.5f, pitch.get());
            pitch.updateAndGet(v -> v + 0.2f);
        }, TaskSchedule.immediate(), TaskSchedule.millis(100));

        scheduler.scheduleTask(task::cancel, TaskSchedule.millis(500), TaskSchedule.stop());
    }
}
