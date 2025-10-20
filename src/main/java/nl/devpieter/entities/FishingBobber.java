package nl.devpieter.entities;

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
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.chunk.ChunkCache;
import nl.devpieter.models.fishing.ICatchable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FishingBobber extends EntityCreature {

    public final Instance instance;

    private final ItemStack rod;
    private final Player thrower;
    private final ICatchable catchable;

    // int waitTimeReductionTicks = (int)(EnchantmentHelper.getFishingTimeReduction(serverWorld, itemStack, user) * 20.0f);
    private final int idleTimeReductionTicks = 0;

    private State state = State.FLYING;
    private PhysicsResult previousPhysicsResult = null;

    private boolean luring;
    private boolean caught;

    private int idleTimer;
    private int hookBiteTimer;
    private int fishSwimTimer;
    private float fishSwimAngle;

    enum State {
        FLYING,
        BOBBING;
    }

    public FishingBobber(@NotNull Player thrower, @NotNull ItemStack rod, @NotNull ICatchable catchable) {
        super(EntityType.FISHING_BOBBER);
        this.instance = thrower.getInstance();
        this.thrower = thrower;
        this.rod = rod;
        this.catchable = catchable;

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
                0.5 / length + nextTriangular(0.5, 0.0103365),
                0.5 / length + nextTriangular(0.5, 0.0103365),
                0.5 / length + nextTriangular(0.5, 0.0103365)
        );

        setVelocity(vec);

        setView(
                (float) (Math.atan2(vec.x(), vec.z()) * (Math.PI / 180)),
                (float) (Math.atan2(vec.y(), vec.length()) * (Math.PI / 180))
        );
    }

    public void reelIn() {

        if (caught) {
            catchable.onCatch(thrower, rod);

            FishingItem itemEntity = new FishingItem(catchable.getItem());
            double x = thrower.getPosition().x() - getPosition().x();
            double y = thrower.getPosition().y() - getPosition().y();
            double z = thrower.getPosition().z() - getPosition().z();

            itemEntity.setInstance(thrower.getInstance(), getPosition());
            itemEntity.setVelocity(new Vec(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1));
        }

        remove();
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

        if (state == State.FLYING) {

            if (collidesWithWater()) {
                setVelocity(getVelocity().mul(0.3, 0.2, 0.3));
                state = State.BOBBING;
            }
        } else {
            if (state == State.BOBBING) {
                double d = getPosition().y() + vel.y() - pos.y() - f;
                if (Math.abs(d) < 0.01) d += Math.signum(d) * 0.1;

                setVelocity(new Vec(
                        vel.x() * 0.9,
                        vel.y() - d * random.nextDouble() * 0.2,
                        vel.z() * 0.9
                ));

                if (collidesWithWater()) {

                    if (caught) {
                        setVelocity(getVelocity().add(0.0, -0.1 * random.nextFloat() * random.nextFloat(), 0.0));
                    }

                    tickFishingLogic();
                }
            }

            if (!collidesWithWater()) {
                setVelocity(getVelocity().add(0.0, -0.03, 0.0));
            }
        }

        if (state == State.FLYING && isOnGround()) {
            setVelocity(Vec.ZERO);
        }

        gravityTickCount = onGround ? 0 : gravityTickCount + 1;

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

    private void tickFishingLogic() {
        Random rng = new Random();
        int tickStep = 1;

        if (hookBiteTimer > 0) {
            --hookBiteTimer;

            if (hookBiteTimer <= 0) {
                idleTimer = 0;
                fishSwimTimer = 0;

//            getDataTracker().set(CAUGHT_FISH, false);
                caught = false;
                catchable.onFail(thrower, rod);

                System.out.println("Ready to catch fish!");
            }
        } else if (fishSwimTimer > 0) {
            fishSwimTimer -= tickStep;

            if (fishSwimTimer > 0) {

                if (!luring) catchable.onLure(thrower, rod);
                luring = true;

                fishSwimAngle += (float) nextTriangular(0.0, 9.188);

                float fishSwimAngleRad = (float) (fishSwimAngle * (Math.PI / 180));
                float sinAngle = (float) Math.sin(fishSwimAngleRad);
                float cosAngle = (float) Math.cos(fishSwimAngleRad);

                double particleX = getPosition().x() + (sinAngle * fishSwimTimer * 0.1f);
                double particleY = Math.floor(getPosition().y()) + 1.0f;
                double particleZ = getPosition().z() + (cosAngle * fishSwimTimer * 0.1f);

//            if (blockState.isOf(Blocks.WATER)) {
                {
                    if (rng.nextFloat() < 0.15f) {
                        sendPacketToViewersAndSelf(new ParticlePacket(
                                Particle.BUBBLE,
                                new Pos(particleX, particleY - 0.1f, particleZ),
                                new Pos(sinAngle, 0.1, cosAngle),
                                0,
                                1
                        ));
                    }

                    float offsetX = cosAngle * 0.04f;
                    float offsetY = 0.01f;
                    float offsetZ = sinAngle * 0.04f;

                    sendPacketToViewersAndSelf(new ParticlePacket(
                            Particle.FISHING,
                            new Pos(particleX, particleY, particleZ),
                            new Pos(offsetX, offsetY, -offsetZ),
                            1.0f,
                            0
                    ));
                    sendPacketToViewersAndSelf(new ParticlePacket(
                            Particle.FISHING,
                            new Pos(particleX, particleY, particleZ),
                            new Pos(-offsetX, offsetY, offsetZ),
                            1.0f,
                            0
                    ));
                }
            } else {
                luring = false;

//            playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f);

                sendPacketToViewersAndSelf(new ParticlePacket(
                        Particle.BUBBLE,
                        new Pos(getPosition().x(), getPosition().y() + 0.5, getPosition().z()),
                        new Pos(getBoundingBox().width(), 0.0, getBoundingBox().width()),
                        0.2f,
                        (int) (1.0f + getBoundingBox().width() * 20.0f)
                ));
                sendPacketToViewersAndSelf(new ParticlePacket(
//                        Particle.FISHING,
                        Particle.HEART,
                        new Pos(getPosition().x(), getPosition().y() + 0.5, getPosition().z()),
                        new Pos(getBoundingBox().width(), 0.0, getBoundingBox().width()),
                        0.2f,
                        (int) (1.0f + getBoundingBox().width() * 20.0f)
                ));

                hookBiteTimer = rng.nextInt(20, 40);

//            getDataTracker().set(CAUGHT_FISH, true);

                caught = true;
                catchable.onBite(thrower, rod);

                setVelocity(new Vec(getVelocity().x(), -0.4f * rng.nextFloat(0.6f, 1.0f), getVelocity().z()));

                System.out.println("Fish is biting! Hook countdown: " + hookBiteTimer);
            }
        } else if (idleTimer > 0) {
            idleTimer -= tickStep;

            float splashChance = 0.15f;
            if (idleTimer < 20) splashChance += (20 - idleTimer) * 0.05f;
            else if (idleTimer < 40) splashChance += (40 - idleTimer) * 0.02f;
            else if (idleTimer < 60) splashChance += (60 - idleTimer) * 0.01f;

            if (rng.nextFloat() < splashChance) {
                float randomAngle = (float) (rng.nextFloat(0.0f, 360.0f) * (Math.PI / 180));
                float randomDistance = rng.nextFloat(25.0f, 60.0f);

                double splashX = getPosition().x() + Math.sin(randomAngle) * randomDistance * 0.1;
                double splashY = Math.floor(getPosition().y()) + 1.0f;
                double splashZ = getPosition().z() + Math.cos(randomAngle) * randomDistance * 0.1;

//            if (blockState.isOf(Blocks.WATER)) {
                {
                    sendPacketToViewersAndSelf(new ParticlePacket(
                            Particle.SPLASH,
                            new Pos(splashX, splashY - 0.1f, splashZ),
                            new Pos(0.1, 0.0, 0.1),
                            0,
                            2 + rng.nextInt(2)
                    ));
                }
            }

            if (idleTimer <= 0) {
                fishSwimAngle = rng.nextFloat(0.0f, 360.0f);
                fishSwimTimer = rng.nextInt(20, 80);
            }
        } else {
//            idleTimer = rng.nextInt(100, 600);
            idleTimer = rng.nextInt(20 * 2, 20 * 5);
            idleTimer -= idleTimeReductionTicks;
        }
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
                    if (block.compare(Block.SEA_PICKLE)) return true;
                }
            }
        }

        return false;
    }

    private double nextTriangular(double mode, double deviation) {
        Random random = new Random();
        return mode + deviation * (random.nextDouble() - random.nextDouble());
    }
}
