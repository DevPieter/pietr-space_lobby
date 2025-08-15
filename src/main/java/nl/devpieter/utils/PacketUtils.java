package nl.devpieter.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.jetbrains.annotations.NotNull;

public class PacketUtils {

    public static int replaceWithGlowing(@NotNull Player player, @NotNull Pos pos) {
        Instance instance = player.getInstance();
        Block block = instance.getBlock(pos);

        EntityCreature glowingEntity = new EntityCreature(EntityType.FALLING_BLOCK);
        glowingEntity.setGlowing(true);
        glowingEntity.setNoGravity(true);

        player.sendPacket(new SpawnEntityPacket(
                glowingEntity.getEntityId(),
                glowingEntity.getUuid(),
                EntityType.FALLING_BLOCK.id(),
                pos,
                0f,
                block.stateId(),
                (short) 0,
                (short) 0,
                (short) 0
        ));

        player.sendPacket(glowingEntity.getMetadataPacket());
        player.sendPacket(new BlockChangePacket(pos, Block.AIR));

        return glowingEntity.getEntityId();
    }

    public static void revertToOriginalBlock(@NotNull Player player, @NotNull Pos pos, int entityId) {
        Instance instance = player.getInstance();
        Block block = instance.getBlock(pos);

        player.sendPacket(new BlockChangePacket(pos, block));
        player.sendPacket(new DestroyEntitiesPacket(entityId));
    }

    public static void setSlot(@NotNull Player player, int slot, @NotNull ItemStack item) {
        player.sendPacket(new SetSlotPacket(
                0, // Window ID (0 for main inventory)
                0, // State ID
                (short) slot,
                item
        ));
    }
}
