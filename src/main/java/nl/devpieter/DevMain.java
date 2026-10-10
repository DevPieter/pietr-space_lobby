package nl.devpieter;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.entities.LobbyPlayer;

public class DevMain {

    private final GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

    public void run() {
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (!(event.getPlayer() instanceof LobbyPlayer lobbyPlayer)) return;
            lobbyPlayer.addItemToMainInventory(ItemStack.of(Material.STICK));
        });

        eventHandler.addListener(PlayerUseItemEvent.class, event -> {
            if (!(event.getPlayer() instanceof LobbyPlayer player)) return;
            if (player.getItemInHand(event.getHand()).material() != Material.STICK) return;
            event.setCancelled(true);

            Instance instance = player.getInstance();

//            Pos posCenter = player.getPosition().add(player.getPosition().direction().mul(3));
//            posCenter = posCenter.withYaw(0).withPitch(0).add(0, 1, 0);

            BlockVec pos1 = new BlockVec(1, 66, 1);
            BlockVec pos2 = new BlockVec(8, 69, 8);

            placeBoxOutline(instance, pos1, pos2);

        });
    }

    private void placeBoxOutline(Instance instance, BlockVec pos1, BlockVec pos2) {
        double minX = Math.min(pos1.x(), pos2.x());
        double minY = Math.min(pos1.y(), pos2.y());
        double minZ = Math.min(pos1.z(), pos2.z());
        double maxX = Math.max(pos1.x(), pos2.x());
        double maxY = Math.max(pos1.y(), pos2.y());
        double maxZ = Math.max(pos1.z(), pos2.z());
        double lineWidth = 0.05;

        // The twelve edges: four along each axis. Scaling a display makes each
        // edge a single entity instead of a chain of small cubes.
        for (double y : new double[]{minY, maxY}) {
            for (double z : new double[]{minZ, maxZ}) {
                placeBlockDisplay(instance, new Vec(minX, y, z), new Vec(maxX - minX, lineWidth, lineWidth));
            }
        }
        for (double x : new double[]{minX, maxX}) {
            for (double z : new double[]{minZ, maxZ}) {
                placeBlockDisplay(instance, new Vec(x, minY, z), new Vec(lineWidth, maxY - minY, lineWidth));
            }
        }
        for (double x : new double[]{minX, maxX}) {
            for (double y : new double[]{minY, maxY}) {
                placeBlockDisplay(instance, new Vec(x, y, minZ), new Vec(lineWidth, lineWidth, maxZ - minZ));
            }
        }
    }

    private Entity placeBlockDisplay(Instance instance, Point pos, Vec size) {
        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setInstance(instance, pos);

        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.WHITE_CONCRETE);
            meta.setScale(size);
            meta.setHasGlowingEffect(true);

            meta.setHasNoGravity(true);
        });

        return entity;
    }
}
