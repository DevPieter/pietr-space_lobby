package nl.devpieter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.structures.StructureManager;
import nl.devpieter.features.structures.StructurePlacer;
import nl.devpieter.features.structures.models.Structure;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.sees.Sees;

public class DevMain {

    private final Sees sees = Sees.getSharedInstance();
    private final GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
    private final StructureManager structureManager = StructureManager.getInstance();
    private final WorldManager worldManager = WorldManager.getInstance();

    public void run() {
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (!(event.getPlayer() instanceof LobbyPlayer lobbyPlayer)) return;
            lobbyPlayer.addItemToMainInventory(ItemStack.of(Material.WOODEN_HOE));
        });

        eventHandler.addListener(PlayerBlockInteractEvent.class, event -> {
            if (!(event.getPlayer() instanceof LobbyPlayer player)) return;
            if (player.getItemInHand(event.getHand()).material() != Material.WOODEN_HOE) return;

            event.setCancelled(true);

            int x = event.getBlockPosition().blockX();
            int y = event.getBlockPosition().blockY();
            int z = event.getBlockPosition().blockZ();

            Structure structure = structureManager.getStructure("testing_horse_1");
            StructurePlacer.placeStructure(
                    event.getInstance(),
                    new BlockVec(x, y + 1, z),
                    structure,
                    StructurePlacer.Rotation.ROTATE_0,
                    StructurePlacer.Mirror.NONE
            );

            SignManager signManager = SignManager.getInstance();
            signManager.scanSigns(event.getInstance(), new BlockVec(x, y + 1, z), structure.nbt().size().asBoundingBox());
        });

//        eventHandler.addListener(PlayerStartSneakingEvent.class, event -> {
//            Player player = event.getPlayer();
//
//            PlayLogger pLogger = PlayLogger.getInstance();
//            pLogger.debug(player, "Testing 123, {} sneaking event {}", player.getName(), 321);
//            pLogger.debug(player, true, "Testing 123, {} sneaking event {}", player.getName(), 321);
//        });
    }
}
