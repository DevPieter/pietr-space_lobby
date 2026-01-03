package nl.devpieter;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.blockHandler.DoorBlockHandler;
import nl.devpieter.blockHandler.SignBlockHandler;
import nl.devpieter.commands.ReplayCommand;
import nl.devpieter.commands.ServerCommand;
import nl.devpieter.commands.TestCommand;
import nl.devpieter.commands.TipCommand;
import nl.devpieter.commands.player.GamemodeCommand;
import nl.devpieter.commands.world.*;
import nl.devpieter.config.ConfigManager;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.features.cooking.blockHandlers.CampfireBlockHandler;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.FishingReferenceHolder;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.features.fishing.listeners.PlayerUseItemListener;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.features.jukebox.blockHandlers.JukeboxBlockHandler;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.ParkourReplayManager;
import nl.devpieter.features.parkour.ParkourTryManager;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.parkour.listeners.ParkourPlayerMoveListener;
import nl.devpieter.features.parkour.listeners.ParkourPlayerSneakListener;
import nl.devpieter.features.portals.listeners.PortalPlayerMoveListener;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.features.sitting.listeners.PlayerBlockInteractListener;
import nl.devpieter.features.sitting.listeners.PlayerStartSneakingListener;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.listeners.PlayerConfigurationListener;
import nl.devpieter.listeners.PlayerDisconnectListener;
import nl.devpieter.listeners.PlayerLoadedListener;
import nl.devpieter.listeners.PlayerMoveListener;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.sees.Sees;
import nl.devpieter.utils.minestom.PlayerUtils;
import nl.devpieter.utils.minestom.SoundUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.slf4j.Logger;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) {
        Logger logger = MinecraftServer.LOGGER;
        if (EnvStatics.DEBUG) logger.warn("Debug mode is enabled");

        ConfigManager configManager = ConfigManager.getInstance();
        configManager.load();

        boolean useVelocity = configManager.getBoolean("use_velocity_proxy");
        String velocitySecret = configManager.getString("velocity_proxy_secret");

//        Auth auth = useVelocity ? new Auth.Velocity(velocitySecret) : new Auth.Online();
        Auth auth = useVelocity ? new Auth.Velocity(velocitySecret) : new Auth.Offline();
        logger.info("Using authentication: {}", useVelocity ? "Velocity" : "Online");

        MinecraftServer server = MinecraftServer.init(auth);
        MinecraftServer.setBrandName("Pieter Space Lobby");

        MinecraftServer.getBlockManager().registerHandler(Key.key("minecraft:campfire"), CampfireBlockHandler::new);
        MinecraftServer.getBlockManager().registerHandler(Key.key("minecraft:jukebox"), JukeboxBlockHandler::new);
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

            playSoundEffect(player);
        });

        Sees sees = Sees.getInstance();
        sees.subscribe(new MainListener());

        WorldManager worldManager = WorldManager.getInstance();
        worldManager.init();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(new PlayerConfigurationListener());
        eventHandler.addListener(new PlayerMoveListener());

        eventHandler.addListener(new PlayerBlockInteractListener());
        eventHandler.addListener(new PlayerStartSneakingListener());
        eventHandler.addListener(new PlayerUseItemListener());

        eventHandler.addListener(new PlayerLoadedListener());
        eventHandler.addListener(new PlayerDisconnectListener());

        eventHandler.addListener(new PortalPlayerMoveListener());
        eventHandler.addListener(new ParkourPlayerMoveListener());
        eventHandler.addListener(new ParkourPlayerSneakListener());

        // TODO - Temporary parkour listeners
        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            if (!(event.getPlayer() instanceof LobbyPlayer lobbyPlayer)) return;
            if (!lobbyPlayer.isDynamicInventoryActive()) return;
            event.setCancelled(true);

            Material material = event.getItemStack().material();

            if (material == Material.SPRUCE_PRESSURE_PLATE) {
                ParkourManager.getInstance().returnToCheckpoint(lobbyPlayer, ParkourFailReason.PlayerRequest);
            } else if (material == Material.BARRIER) {
                ParkourManager.getInstance().cancelParkour(lobbyPlayer, ParkourCancelReason.PlayerRequest);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
            if (!(event.getPlayer() instanceof LobbyPlayer lobbyPlayer)) return;
            if (!lobbyPlayer.isDynamicInventoryActive()) return;
            event.setCancelled(true);

            PlayerHand hand = event.getHand();
            if (hand != PlayerHand.MAIN) return;

            Material material = event.getPlayer().getItemInHand(hand).material();

            if (material == Material.SPRUCE_PRESSURE_PLATE) {
                ParkourManager.getInstance().returnToCheckpoint(lobbyPlayer, ParkourFailReason.PlayerRequest);
            } else if (material == Material.BARRIER) {
                ParkourManager.getInstance().cancelParkour(lobbyPlayer, ParkourCancelReason.PlayerRequest);
            }
        });

        // TODO - Temporary farming replanting listener
        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartDiggingEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);

            Block block = event.getBlock();
            BlockHandler handler = block.handler();
            Instance instance = event.getInstance();

            if (!(handler instanceof FarmBlockHandler farmHandler)) return;

            BlockHolder holder = new BlockHolder(instance, event.getBlockPosition(), block);
            if (!farmHandler.canBeHarvested(holder)) return;

            farmHandler.replant(holder, event.getPlayer());
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartDiggingEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
        });

        eventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
        });

        eventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerTickEvent.class, event -> {
            Player player = event.getPlayer();

            boolean isSprinting = player.isSprinting();
            boolean hasLowHeadroom = PlayerUtils.hasLowHeadroom(player);
            boolean isInWater = PlayerUtils.isPlayerInWater(player);

            if (isInWater && hasLowHeadroom && isSprinting) {
                player.setPose(EntityPose.SWIMMING);
            }

            if (!(isInWater && isSprinting)) {
                if (player.getPose() == EntityPose.SWIMMING) {
                    player.setPose(EntityPose.STANDING);
                }
            }
        });

//        eventHandler.addListener(PlayerStartSneakingEvent.class, event -> {
//            Player player = event.getPlayer();
//            PackManager packManager = PackManager.getInstance();
//
//            packManager.tryInformRecommendation(player, false);
//
////            if(packManager.hasAcceptedPack(player)) {
//                System.out.println("Player " + player.getUsername() + " has accepted the resource pack.");
//
//                Sound sound = Sound.sound(
//                        Key.key("lobby","music.joost.full-circle"),
//                        Sound.Source.RECORD,
//                        1.0f,
//                        1.0f
//                );
//
//                player.getInstance().playSound(sound, player.getPosition());
////            }
//        });

        eventHandler.addListener(PlayerResourcePackStatusEvent.class, event -> {
            Player player = event.getPlayer();
            PackManager packManager = PackManager.getInstance();

            packManager.updateStatus(player, event.getStatus());
        });

        CommandManager commandManager = MinecraftServer.getCommandManager();

        // Player
        commandManager.register(new GamemodeCommand());

        // World
        commandManager.register(new WorldCommand());
        commandManager.register(new SignCommand());
        commandManager.register(new CreatureCommand());
        commandManager.register(new FarmCommand());
        commandManager.register(new ParkourCommand());
        commandManager.register(new PortalCommand());

        commandManager.register(new TimeCommand());
        commandManager.register(new WeatherCommand());

        // Misc
        commandManager.register(new ReplayCommand());
        commandManager.register(new TipCommand());
        commandManager.register(new TestCommand());
        commandManager.register(new ServerCommand());

        MinecraftServer.getConnectionManager().setPlayerProvider(LobbyPlayer::new);

        ParkourTryManager.getInstance().init();
        ParkourReplayManager.getInstance().init();
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
