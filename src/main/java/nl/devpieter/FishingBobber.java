package nl.devpieter;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkCache;

import java.util.Random;

public class FishingBobber extends EntityCreature {

    public boolean inWater = false;
    public final Instance instance;

    private State state = State.FLYING;

    enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }

    public FishingBobber(Player thrower, Instance instance) {
        super(EntityType.FISHING_BOBBER);
        this.instance = instance;

        FishingHookMeta meta = (FishingHookMeta) getEntityMeta();
        meta.setOwnerEntity(thrower);

        Pos loc = thrower.getPosition();
        float pitch = loc.pitch();
        float yaw = loc.yaw();

        double yawCos = Math.cos(-yaw * (Math.PI / 180) - Math.PI);
        double yawSin = Math.sin(-yaw * (Math.PI / 180) - Math.PI);

        double pitchCos = -Math.cos(-pitch * (Math.PI / 180));
        double pitchSin = Math.sin(-pitch * (Math.PI / 180));

        double x = loc.x() - yawSin * 0.3;
        double y = loc.y() + thrower.getEyeHeight();
        double z = loc.z() - yawCos * 0.3;

        setPositionInternal(new Pos(x, y, z, yaw, pitch), 0);

        Vec vec = new Vec(-yawSin, Math.clamp(-(pitchSin / pitchCos), -5.0f, 5.0f), -yawCos);
        double length = vec.length();

        vec = vec.mul(
                0.6 / length + nextTriangular(0.5, 0.0103365),
                0.6 / length + nextTriangular(0.5, 0.0103365),
                0.6 / length + nextTriangular(0.5, 0.0103365)
        );

        setVelocity(vec);

        setView(
                (float) (Math.atan2(vec.x(), vec.z()) * (Math.PI / 180)),
                (float) (Math.atan2(vec.y(), vec.length()) * (Math.PI / 180))
        );
    }

    public double nextTriangular(double mode, double deviation) {
        Random random = new Random();
        return mode + deviation * (random.nextDouble() - random.nextDouble());
    }

    @Override
    public void tick(long time) {
        super.tick(time);
    }

    @Override
    protected void movementTick() {
        Random random = new Random();

        float f = 0.0f;

        Pos pos = getPosition();
        Vec vel = getVelocity();

        if (collidesWithWater()) {
            f = 0.5f;
        }

        if (this.state == State.FLYING) {

            if (collidesWithWater()) {
                this.setVelocity(this.getVelocity().mul(0.3, 0.2, 0.3));
                this.state = State.BOBBING;
            }
        } else {
            if (this.state == State.BOBBING) {
                double d = getPosition().y() + vel.y() - pos.y() - f;
                if (Math.abs(d) < 0.01) d += Math.signum(d) * 0.1;

                this.setVelocity(new Vec(
                        vel.x() * 0.9,
                        vel.y() - d * random.nextDouble() * 0.2,
                        vel.z() * 0.9
                ));
            }

            if (!collidesWithWater()) {
                this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
            }
        }

        if (this.state == State.FLYING && this.isOnGround()) {
            this.setVelocity(Vec.ZERO);
        }

        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;

        Block.Getter chunkCache = new ChunkCache(instance, currentChunk, Block.STONE);
        PhysicsResult physicsResult = PhysicsUtils.simulateMovement(
                position,
                getVelocity().mul(0.92),
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

    private PhysicsResult previousPhysicsResult = null;

    @Override
    public void remove() {
        super.remove();
    }

    private boolean collidesWithWater() {
        BoundingBox bb = getBoundingBox();

        Pos min = getPosition().add(bb.minX(), bb.minY(), bb.minZ());
        Pos max = getPosition().add(bb.maxX(), bb.maxY(), bb.maxZ());

        int minX = (int) Math.floor(min.x());
        int minY = (int) Math.floor(min.y());

        int minZ = (int) Math.floor(min.z());
        int maxX = (int) Math.floor(max.x());

        int maxY = (int) Math.floor(max.y());
        int maxZ = (int) Math.floor(max.z());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = instance.getBlock(x, y, z);
                    if (block.compare(Block.WATER)) return true;
                    if (block.compare(Block.KELP)) return true;
                    if (block.compare(Block.KELP_PLANT)) return true;
                    if (block.compare(Block.SEAGRASS)) return true;
                    if (block.compare(Block.TALL_SEAGRASS)) return true;
                }
            }
        }

        return false;
    }
}
