package nl.devpieter.models.fishing.catchable;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.entities.FishingBobber;
import nl.devpieter.models.fishing.rod.IRod;

public class CodCatchable implements ICatchable {

    @Override
    public Component getName() {
        return Component.text("Cod");
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.of(Material.COD).withCustomName(getName());
    }

    @Override
    public double getPickChance(Player player, IRod rod) {
        return 50.0;
    }

    @Override
    public void onLure(Player player, IRod rod, FishingBobber bobber) {
    }

    @Override
    public void onBite(Player player, IRod rod, FishingBobber bobber) {
    }

    @Override
    public void onCatch(Player player, IRod rod, FishingBobber bobber) {
        reelInItem(player, bobber);
    }

    @Override
    public void onFail(Player player, IRod rod, FishingBobber bobber) {
    }
}
