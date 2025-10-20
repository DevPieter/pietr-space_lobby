package nl.devpieter;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.commands.GamemodeCommand;
import nl.devpieter.commands.TestCommand;
import nl.devpieter.commands.fun.LaunchCommand;
import nl.devpieter.features.farming.listeners.PlayerBlockBreakListener;
import nl.devpieter.features.fishing.listeners.PlayerUseItemListener;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.parkour.enums.ParkourFailReason;
import nl.devpieter.features.sitting.listeners.PlayerBlockInteractListener;
import nl.devpieter.features.sitting.listeners.PlayerStartSneakingListener;
import nl.devpieter.listeners.*;
import nl.devpieter.features.parkour.listeners.ParkourPlayerMoveListener;
import nl.devpieter.features.portals.listeners.PortalPlayerMoveListener;
import nl.devpieter.features.farming.FarmManager;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.ParkourTryManager;
import nl.devpieter.managers.WorldManager;
import nl.devpieter.utils.PacketUtils;
import org.slf4j.Logger;

public class Main {

    public static void main(String[] args) {

        Logger logger = MinecraftServer.LOGGER;
        if (Settings.DEBUG) logger.warn("Debug mode is enabled");

        boolean useVelocity = Settings.USE_VELOCITY_PROXY;
        String velocitySecret = Settings.VELOCITY_PROXY_SECRET;

        Auth auth = useVelocity ? new Auth.Velocity(velocitySecret) : new Auth.Online();
        logger.info("Using authentication: {}", useVelocity ? "Velocity" : "Online");

        MinecraftServer server = MinecraftServer.init(auth);
        MinecraftServer.setBrandName("Pieter Space Lobby");

        WorldManager worldManager = WorldManager.getInstance();
        worldManager.loadWorlds();

        ParkourManager parkourManager = ParkourManager.getInstance();
        parkourManager.init();

        FarmManager farmManager = FarmManager.getInstance();
        farmManager.init();

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

        eventHandler.addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new LaunchCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new TestCommand());

        ParkourTryManager.getInstance().start();
        server.start("0.0.0.0", 25565);
    }
}
