package nl.devpieter.features.fishing.catchable;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.rod.IRod;

public class SalmonCatchable implements ICatchable {

    @Override
    public Component getName() {
        return Component.text("Salmon");
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.of(Material.SALMON).withCustomName(getName());
    }

    @Override
    public double getPickChance(Player player, IRod rod) {
        return 50.0;
    }

    @Override
    public void onLure(Player player, IRod rod, FishingBobber bobber) {
//        player.sendMessage(Component.text("You feel a tug on your line..."));
    }

    @Override
    public void onBite(Player player, IRod rod, FishingBobber bobber) {
//        player.sendMessage(Component.text("A salmon is nibbling on your hook!"));
    }

    @Override
    public void onCatch(Player player, IRod rod, FishingBobber bobber) {
//        player.sendMessage(Component.text("You caught a salmon!"));
        reelInItem(player, bobber);
    }

    @Override
    public void onFail(Player player, IRod rod, FishingBobber bobber) {
//        player.sendMessage(Component.text("The salmon got away!"));
    }
}
