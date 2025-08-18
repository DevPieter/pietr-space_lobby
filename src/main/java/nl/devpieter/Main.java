package nl.devpieter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.enums.ParkourCancelReason;
import nl.devpieter.listeners.*;
import nl.devpieter.listeners.parkour.ParkourPlayerMoveListener;
import nl.devpieter.listeners.portal.PortalPlayerMoveListener;
import nl.devpieter.managers.ParkourManager;
import nl.devpieter.managers.ParkourTryManager;
import nl.devpieter.managers.WorldManager;
import nl.devpieter.utils.PacketUtils;
import org.slf4j.Logger;

public class Main {

    public static void main(String[] args) {

        Logger logger = MinecraftServer.LOGGER;
        if (Settings.DEBUG) logger.warn("Debug mode is enabled");

        MinecraftServer server = MinecraftServer.init();
        MinecraftServer.setBrandName("Pieter Space Lobby");

        WorldManager worldManager = WorldManager.getInstance();
        worldManager.loadWorlds();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(new PlayerConfigurationListener());
        eventHandler.addListener(new PlayerMoveListener());

        eventHandler.addListener(new PlayerBlockInteractListener());
        eventHandler.addListener(new PlayerStartSneakingListener());

        eventHandler.addListener(new PlayerLoadedListener());
        eventHandler.addListener(new PlayerDisconnectListener());

        eventHandler.addListener(new PortalPlayerMoveListener());
        eventHandler.addListener(new ParkourPlayerMoveListener());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            int slot = player.getHeldSlot();

            if (slot == 0) ParkourManager.getInstance().returnToCheckpoint(player);
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

        eventHandler
                .addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true))
                .addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));

        ParkourTryManager.getInstance().start();

        if (Settings.USE_VELOCITY_PROXY) {
            VelocityProxy.enable(Settings.VELOCITY_PROXY_SECRET);
            logger.info("Using Velocity Proxy");
        } else {
            MojangAuth.init();
            logger.info("Using Mojang Auth");
        }

        server.start("0.0.0.0", 25565);
    }
}
