package nl.devpieter.models.fishing;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import nl.devpieter.entities.FishingBobber;
import nl.devpieter.entities.PhysicsItem;
import org.jetbrains.annotations.NotNull;

public interface ICatchable {

    Component getName();

    ItemStack getItem();

    double getCatchChance(Player player, ItemStack rod);

    void onLure(Player player, ItemStack rod, FishingBobber bobber);

    void onBite(Player player, ItemStack rod, FishingBobber bobber);

    void onCatch(Player player, ItemStack rod, FishingBobber bobber);

    void onFail(Player player, ItemStack rod, FishingBobber bobber);

    default void reelInItem(@NotNull Player player, @NotNull FishingBobber bobber) {
        PhysicsItem itemEntity = new PhysicsItem(getItem());

        Pos pPos = player.getPosition();
        Pos bPos = bobber.getPosition();

        double x = pPos.x() - bPos.x();
        double y = pPos.y() - bPos.y();
        double z = pPos.z() - bPos.z();

        itemEntity.setInstance(player.getInstance(), bPos);
        itemEntity.setVelocity(new Vec(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1));
    }
}
