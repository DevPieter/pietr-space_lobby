package nl.devpieter.utils.minestom;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.Either;
import nl.devpieter.models.GlowingBlockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketUtils {

    public static GlowingBlockHolder replaceWithGlowing(@NotNull Player player, @NotNull Pos pos) {
        Instance instance = player.getInstance();
        Block block = instance.getBlock(pos);

        Entity glowingEntity = new Entity(EntityType.FALLING_BLOCK);
        glowingEntity.setGlowing(true);
        glowingEntity.setNoGravity(true);

        player.sendPacket(new SpawnEntityPacket(
                glowingEntity.getEntityId(),
                glowingEntity.getUuid(),
                EntityType.FALLING_BLOCK,
                pos,
                0f,
                block.stateId(),
                Vec.ZERO
        ));

        player.sendPacket(glowingEntity.getMetadataPacket());
        player.sendPacket(new BlockChangePacket(pos, Block.AIR));

        return new GlowingBlockHolder(glowingEntity.getEntityId(), pos);
    }

    public static void revertToOriginalBlock(@NotNull Player player, @NotNull GlowingBlockHolder holder) {
        revertToOriginalBlock(player, holder.entityPos(), holder.entityId());
    }

    public static void revertToOriginalBlock(@NotNull Player player, @NotNull Pos pos, int entityId) {
        Instance instance = player.getInstance();
        Block block = instance.getBlock(pos);

        player.sendPacket(new BlockChangePacket(pos, block));
        player.sendPacket(new DestroyEntitiesPacket(entityId));
    }

    public static void setWaypoint(@NotNull Player player, UUID waypointId, @NotNull Pos pos) {
        player.sendPacket(new TrackedWaypointPacket(
                TrackedWaypointPacket.Operation.TRACK,
                new TrackedWaypointPacket.Waypoint(
                        Either.left(waypointId),
                        TrackedWaypointPacket.Icon.DEFAULT,
                        new TrackedWaypointPacket.Target.Vec3i(pos)
                )
        ));
    }

    public static void updateWaypoint(@NotNull Player player, UUID waypointId, @NotNull Pos pos) {
        player.sendPacket(new TrackedWaypointPacket(
                TrackedWaypointPacket.Operation.UPDATE,
                new TrackedWaypointPacket.Waypoint(
                        Either.left(waypointId),
                        TrackedWaypointPacket.Icon.DEFAULT,
                        new TrackedWaypointPacket.Target.Vec3i(pos)
                )
        ));
    }

    public static void removeWaypoint(@NotNull Player player, UUID waypointId) {
        player.sendPacket(new TrackedWaypointPacket(
                TrackedWaypointPacket.Operation.UNTRACK,
                new TrackedWaypointPacket.Waypoint(
                        Either.left(waypointId),
                        TrackedWaypointPacket.Icon.DEFAULT,
                        new TrackedWaypointPacket.Target.Vec3i(new Pos(0, 0, 0))
                )
        ));
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
