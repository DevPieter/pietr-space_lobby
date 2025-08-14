package nl.devpieter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.listeners.*;
import nl.devpieter.managers.AdvancementService;
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
                Pos portalPosition = new Pos(2, 58, -16);

                if (playerPosition.distance(portalPosition) < 1) {
                    player.setInstance(specialWorld, Settings.SPECIAL_WORLD_SPAWN);
//                    AdvancementService.getInstance().grant(player, "myserver:explore_the_nether");
                }
            } else if (instance == specialWorld) {
                Pos playerPosition = player.getPosition();
                Pos portalPosition = new Pos(-9, 64, 2);

                if (playerPosition.distance(portalPosition) < 1) {
                    player.setInstance(mainWorld, Settings.MAIN_WORLD_SPAWN);
                }
            }
        });


//        VelocityProxy.enable(Settings.VELOCITY_PROXY_SECRET);
//        server.start("0.0.0.0", 25565);

        MojangAuth.init();
        server.start("127.0.0.1", 25565);
    }
}

//        eventHandler
//                .addListener(PlayerLoadedEvent.class, event -> {
//                    var advancementManager = MinecraftServer.getAdvancementManager();
//
//                    var mainRoot = new AdvancementRoot(
//                            Component.text("Pietr Space"),
//                            Component.text("Welcome to Pietr Space!"),
//                            Material.BLUE_STAINED_GLASS,
//                            FrameType.CHALLENGE,
//                            0, 0,
//                            "minecraft:gui/advancements/backgrounds/stone"
//                    );
//
//                    mainRoot.setAchieved(true);
//
//                    var specialRoot = new AdvancementRoot(
//                            Component.text("Special"),
//                            Component.text("Wow, how did you even get this?"),
//                            Material.DIAMOND_SWORD,
//                            FrameType.GOAL,
//                            0, 0,
//                            "minecraft:gui/advancements/backgrounds/stone"
//                    );
//
//                    specialRoot.setAchieved(true);
//
//                    var advancementTab = advancementManager.createTab("pietr_space_lobby:advancement.main.root", mainRoot);
//
//                    var joinLobbyAdvancement = new Advancement(
//                            Component.text("Join the Lobby"),
//                            Component.text("You have joined the lobby!"),
//                            Material.NETHER_STAR,
//                            FrameType.TASK,
//                            1, 0
//                    );
//
//                    joinLobbyAdvancement.setAchieved(true);
//
//                    advancementTab.createAdvancement("pietr_space_lobby:advancement.main.join_lobby", joinLobbyAdvancement, mainRoot);
//
//                    advancementTab.createAdvancement(
//                            "pietr_space_lobby:advancement.main.explore_the_void",
//                            new Advancement(
//                                    Component.text("Explore the Void"),
//                                    Component.text("Its so empty down here, isn't it?"),
//                                    Material.FEATHER,
//                                    FrameType.CHALLENGE,
//                                    2, 1
//                            ),
//                            joinLobbyAdvancement
//                    );
//
//                    advancementTab.createAdvancement(
//                            "pietr_space_lobby:advancement.main.explore_the_nether",
//                            new Advancement(
//                                    Component.text("Explore the Nether"),
//                                    Component.text("A little too hot for my liking."),
//                                    Material.NETHER_BRICK,
//                                    FrameType.CHALLENGE,
//                                    2, -1
//                            ),
//                            joinLobbyAdvancement
//                    );
//
//                    var specialItemTab = advancementManager.createTab("pietr_space_lobby:advancement.special.root", specialRoot);
//
//                    advancementTab.addViewer(event.getPlayer());
//                    specialItemTab.addViewer(event.getPlayer());
//                });