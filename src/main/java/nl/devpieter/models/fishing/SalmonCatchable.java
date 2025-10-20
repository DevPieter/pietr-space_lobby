package nl.devpieter.models.fishing;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.entities.FishingBobber;

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
    public double getCatchChance(Player player, ItemStack rod) {
        return 0.15;
    }

    @Override
    public void onLure(Player player, ItemStack rod, FishingBobber bobber) {
        player.sendMessage(Component.text("You feel a tug on your line..."));
    }

    @Override
    public void onBite(Player player, ItemStack rod, FishingBobber bobber) {
        player.sendMessage(Component.text("A salmon is nibbling on your hook!"));
    }

    @Override
    public void onCatch(Player player, ItemStack rod, FishingBobber bobber) {
        player.sendMessage(Component.text("You caught a salmon!"));
        reelInItem(player, bobber);
    }

    @Override
    public void onFail(Player player, ItemStack rod, FishingBobber bobber) {
        player.sendMessage(Component.text("The salmon got away!"));
    }
}
