package nl.devpieter;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class FishingBobber extends EntityCreature {

    public boolean inWater = false;
    public final Instance instance;

    public FishingBobber(Player owner, Instance instance, Pos spawnPos, Vec initialVelocity) {
        super(EntityType.FISHING_BOBBER);
        this.instance = instance;

        FishingHookMeta meta = (FishingHookMeta) getEntityMeta();
        meta.setOwnerEntity(owner);

        setInstance(instance, spawnPos);
        setVelocity(initialVelocity);
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (!collidesWith(Block.WATER)) {
            inWater = false;
            return;
        }

        if (!inWater) {
            inWater = true;
            setVelocity(getVelocity().withX(0).withZ(0));
            System.out.println("Bobber is now in water: " + getUuid());
        }

        // Slightly adjust Y velocity to prevent bobber from sinking
        setVelocity(getVelocity().withY(0.00001));
    }

    @Override
    public void remove() {
        super.remove();
    }

    private boolean collidesWith(Block toCheck) {
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
                    if (block.compare(toCheck)) return true;
                }
            }
        }

        return false;
    }
}
