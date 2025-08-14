package nl.devpieter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.listeners.*;
import nl.devpieter.managers.WorldManager;
import org.slf4j.Logger;

public class Main {

    private static final Logger logger = MinecraftServer.LOGGER;

    public static void main(String[] args) {

        MinecraftServer server = MinecraftServer.init();

        WorldManager worldManager = WorldManager.getInstance();
        worldManager.loadWorlds();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(new PlayerConfigurationListener());
        eventHandler.addListener(new PlayerMoveListener());

        eventHandler.addListener(new PlayerBlockInteractListener());
        eventHandler.addListener(new PlayerStartSneakingListener());

        eventHandler.addListener(new PlayerLoadedListener());
        eventHandler.addListener(new PlayerDisconnectListener());

        eventHandler
                .addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true))
                .addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));

        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            InstanceContainer mainWorld = worldManager.getMainWorld();
            InstanceContainer specialWorld = worldManager.getSpecialWorld();

            if (instance == mainWorld) {
                Pos playerPosition = player.getPosition();

                Pos portalStart = new Pos(3, 60, -2);
                Pos portalEnd = new Pos(1, 58, -2);

                if (isBetweenCoordinates(playerPosition, portalStart, portalEnd, 1)) {
                    player.setInstance(specialWorld, new Pos(2.5, 58, -2.5, -180, 0));
                }

            } else if (instance == specialWorld) {
                Pos playerPosition = player.getPosition();

                Pos portalStart = new Pos(3, 60, -2);
                Pos portalEnd = new Pos(1, 58, -2);

                if (isBetweenCoordinates(playerPosition, portalStart, portalEnd, 1)) {
                    player.setInstance(mainWorld, new Pos(2.5, 58, -2.5, -180, 0));
                }
            }
        });

        VelocityProxy.enable(Settings.VELOCITY_PROXY_SECRET);
        server.start("0.0.0.0", 25565);

//        MojangAuth.init();
//        server.start("127.0.0.1", 25565);
    }

    private static boolean isBetweenCoordinates(Pos pos, Pos start, Pos end, double tolerance) {
        double minX = Math.min(start.x(), end.x());
        double maxX = Math.max(start.x(), end.x()) + tolerance;

        double minY = Math.min(start.y(), end.y());
        double maxY = Math.max(start.y(), end.y()) + tolerance;

        double minZ = Math.min(start.z(), end.z());
        double maxZ = Math.max(start.z(), end.z()) + tolerance;

        return pos.x() >= minX && pos.x() <= maxX &&
                pos.y() >= minY && pos.y() <= maxY &&
                pos.z() >= minZ && pos.z() <= maxZ;
    }
}
