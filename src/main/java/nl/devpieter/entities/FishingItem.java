package nl.devpieter.entities;

import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.chunk.ChunkCache;

public class FishingItem extends ItemEntity {

    private PhysicsResult previousPhysicsResult = null;

    private boolean previousOnGround = false;

    public FishingItem(ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;

        Block.Getter chunkCache = new ChunkCache(instance, currentChunk, Block.STONE);
        PhysicsResult physicsResult = PhysicsUtils.simulateMovement(
                position,
                getVelocity(),
                boundingBox,
                instance.getWorldBorder(),
                chunkCache,
                getAerodynamics(),
                hasNoGravity(),
                hasPhysics,
                onGround,
                false,
                previousPhysicsResult
        );

        previousPhysicsResult = physicsResult;
        setVelocity(physicsResult.newVelocity());

        onGround = physicsResult.isOnGround();
        refreshPosition(physicsResult.newPosition(), true);
    }
}
