package nl.devpieter.features.fishing.catchable;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import nl.devpieter.entities.PhysicsItem;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.FishingReferenceHolder;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import org.jetbrains.annotations.NotNull;

public abstract class Catchable implements ICatchable {

    protected abstract ItemStack getItemStack();

    @Override
    public ItemStack buildItem() {
        return getItemStack().with(builder -> {
            builder.setTag(Tag.UUID("catchable_id"), getId());
            builder.customName(getName());
        });
    }

    protected void reelInItem(@NotNull Player player, @NotNull FishingBobber bobber) {
        PhysicsItem itemEntity = new PhysicsItem(buildItem());

        Pos pPos = player.getPosition();
        Pos bPos = bobber.getPosition();

        double x = pPos.x() - bPos.x();
        double y = pPos.y() - bPos.y();
        double z = pPos.z() - bPos.z();

        itemEntity.setInstance(player.getInstance(), bPos);
        itemEntity.setVelocity(new Vec(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1));

        FishingManager.getInstance().registerReference(itemEntity, new FishingReferenceHolder(
                player,
                itemEntity,
                bobber.getRod(),
                this
        ));
    }
}
