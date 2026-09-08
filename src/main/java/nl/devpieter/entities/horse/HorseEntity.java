package nl.devpieter.entities.horse;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.animal.HorseMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;

public class HorseEntity extends EntityCreature {

    public HorseEntity() {
        super(EntityType.HORSE);

        HorseMeta meta = (HorseMeta) getEntityMeta();
        meta.setVariantAndMarking(HorseMeta.Variant.BLACK, HorseMeta.Marking.WHITE);

        meta.setTamed(true);
        setEquipment(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE));
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(1.0);
        getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(1.0);
    }

    @Override
    public void movementTick() {
        if (getControllingPlayer() == null) {
            super.movementTick();
            return;
        }

        velocity = Vec.ZERO;
        gravityTickCount = 0;
    }

    @Override
    public void sendPacketToViewers(SendablePacket packet) {
        Player rider = getControllingPlayer();

        if (rider != null && packet instanceof EntityVelocityPacket) {
            for (Player viewer : getViewers()) if (viewer != rider) viewer.sendPacket(packet);
            return;
        }

        super.sendPacketToViewers(packet);
    }

    private Player getControllingPlayer() {
        var passengers = getPassengers();
        return !passengers.isEmpty() && passengers.getFirst() instanceof Player player ? player : null;
    }
}
