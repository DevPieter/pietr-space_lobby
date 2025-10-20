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
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.chunk.ChunkCache;
import nl.devpieter.enums.BobberState;
import nl.devpieter.models.fishing.BobberSettings;
import nl.devpieter.models.fishing.catchable.ICatchable;
import nl.devpieter.models.fishing.rod.IRod;
import nl.devpieter.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class FishingBobber extends EntityCreature implements IBobber {

    public final Instance instance;

    private final IRod rod;
    private final Player thrower;
    private final ICatchable catchable;

    // int waitTimeReductionTicks = (int)(EnchantmentHelper.getFishingTimeReduction(serverWorld, itemStack, user) * 20.0f);
    private final int idleTimeReductionTicks = 0;

    private BobberState state = BobberState.FLYING;
    private PhysicsResult previousPhysicsResult = null;

    private boolean luring;
    private boolean caught;

    private int idleTimer;
    private int hookBiteTimer;
    private int fishSwimTimer;
    private float fishSwimAngle;

    public FishingBobber(@NotNull Player thrower, @NotNull IRod rod, @Nullable ICatchable catchable) {
        super(EntityType.FISHING_BOBBER);

        this.instance = thrower.getInstance();
        this.thrower = thrower;
        this.rod = rod;
        this.catchable = catchable;

        FishingHookMeta meta = (FishingHookMeta) getEntityMeta();
        meta.setOwnerEntity(thrower);
    }

    @Override
    public EntityCreature getEntity() {
        return this;
    }

    @Override
    public Player getThrower() {
        return thrower;
    }

    @Override
    public @Nullable ICatchable getCatchable() {
        return catchable;
    }

    @Override
    public IRod getRod() {
        return rod;
    }

    @Override
    public void reelIn() {
        if (caught && catchable != null) catchable.onCatch(thrower, rod, this);
        remove();
    }

    public void setPosInternal(Pos newPosition, float headRotation) {
        setPositionInternal(newPosition, headRotation);
    }

    @Override
    protected void movementTick() {
        Random random = new Random();
        float f = 0.0f;

        Pos pos = getPosition();
        Vec vel = getVelocity();

        if (collidesWithWater(getPosition(), getBoundingBox())) {
            f = 0.5f;
        }

        if (state == BobberState.FLYING) {

            if (collidesWithWater(getPosition(), getBoundingBox())) {
                setVelocity(getVelocity().mul(0.3, 0.2, 0.3));
                state = BobberState.BOBBING;
            }
        } else {
            if (state == BobberState.BOBBING) {
                double d = getPosition().y() + vel.y() - pos.y() - f;
                if (Math.abs(d) < 0.01) d += Math.signum(d) * 0.1;

                setVelocity(new Vec(
                        vel.x() * 0.9,
                        vel.y() - d * random.nextDouble() * 0.2,
                        vel.z() * 0.9
                ));

                if (collidesWithWater(getPosition(), getBoundingBox())) {

                    if (caught) {
                        setVelocity(getVelocity().add(0.0, -0.1 * random.nextFloat() * random.nextFloat(), 0.0));
                    }

                    tickFishingLogic();
                }
            }

            if (!collidesWithWater(getPosition(), getBoundingBox())) {
                setVelocity(getVelocity().add(0.0, -0.03, 0.0));
            }
        }

        if (state == BobberState.FLYING && isOnGround()) {
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
        BobberSettings settings = rod.getBobberSettings();

        // TODO - Adjust step based on world events, such as rain
        int tickStep = 1;

        if (hookBiteTimer > 0) {
            --hookBiteTimer;

            if (hookBiteTimer <= 0) {
                idleTimer = 0;
                fishSwimTimer = 0;

                caught = false;
                if (catchable != null) catchable.onFail(thrower, rod, this);
            }
        } else if (fishSwimTimer > 0) {
            fishSwimTimer -= tickStep;

            if (fishSwimTimer > 0) {
                if (!luring && catchable != null) catchable.onLure(thrower, rod, this);
                luring = true;

                fishSwimAngle += (float) MathUtils.nextTriangular(0.0, 9.188);

                float fishSwimAngleRad = (float) (fishSwimAngle * (Math.PI / 180));
                float sinAngle = (float) Math.sin(fishSwimAngleRad);
                float cosAngle = (float) Math.cos(fishSwimAngleRad);

                double particleX = getPosition().x() + (sinAngle * fishSwimTimer * 0.1f);
                double particleY = Math.floor(getPosition().y()) + 1.0f;
                double particleZ = getPosition().z() + (cosAngle * fishSwimTimer * 0.1f);

//            if (blockState.isOf(Blocks.WATER)) {
                {
                    if (rng.nextFloat() < 0.15f) sendPacketToViewersAndSelf(new ParticlePacket(
                            Particle.BUBBLE,
                            new Pos(particleX, particleY - 0.1f, particleZ),
                            new Pos(sinAngle, 0.1f, cosAngle),
                            0,
                            1
                    ));

                    float offsetX = cosAngle * 0.04f;
                    float offsetZ = sinAngle * 0.04f;

                    sendPacketToViewersAndSelf(new ParticlePacket(
                            Particle.FISHING,
                            new Pos(particleX, particleY, particleZ),
                            new Pos(offsetX, 0.01f, -offsetZ),
                            1.0f,
                            0
                    ));
                    sendPacketToViewersAndSelf(new ParticlePacket(
                            Particle.FISHING,
                            new Pos(particleX, particleY, particleZ),
                            new Pos(-offsetX, 0.01f, offsetZ),
                            1.0f,
                            0
                    ));
                }
            } else {
                luring = false;

//            playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f);

                Pos particlePos = new Pos(getPosition().x(), getPosition().y() + 0.5, getPosition().z());
                Pos particleOffset = new Pos(getBoundingBox().width(), 0.0, getBoundingBox().width());

                sendPacketToViewersAndSelf(new ParticlePacket(
                        Particle.BUBBLE,
                        particlePos,
                        particleOffset,
                        0.2f,
                        (int) (1.0f + getBoundingBox().width() * 20.0f)
                ));
                sendPacketToViewersAndSelf(new ParticlePacket(
                        Particle.FISHING,
                        particlePos,
                        particleOffset,
                        0.2f,
                        (int) (1.0f + getBoundingBox().width() * 20.0f)
                ));

                hookBiteTimer = rng.nextInt(settings.minBiteTime(), settings.maxBiteTime());

                caught = true;
                if (catchable != null) catchable.onBite(thrower, rod, this);

                setVelocity(new Vec(getVelocity().x(), -0.4f * rng.nextFloat(0.6f, 1.0f), getVelocity().z()));
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
                fishSwimTimer = rng.nextInt(settings.minLureTime(), settings.maxLureTime());
            }
        } else {
            idleTimer = rng.nextInt(settings.minIdleTime(), settings.maxIdleTime());
            idleTimer -= idleTimeReductionTicks;
        }
    }

    // TODO - Optimize
    private boolean collidesWithWater(@NotNull Pos pos, @NotNull BoundingBox box) {
        Pos min = pos.add(box.minX(), box.minY(), box.minZ());
        Pos max = pos.add(box.maxX(), box.maxY(), box.maxZ());

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
}
