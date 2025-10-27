package nl.devpieter.features.fishing.catchable;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.utils.TextUtils;

import java.util.UUID;

public class CodCatchable extends Catchable {

    @Override
    public UUID getId() {
        return UUID.fromString("be321241-18de-48eb-aa3f-8ac8d2d6e6aa");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#cbac82:#d9cbb6>Cod");
    }

    @Override
    protected ItemStack getItemStack() {
        return ItemStack.of(Material.COD);
    }

    @Override
    public double getPickChance(Player player, IRod rod) {
        return 40.0;
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

    @Override
    public void onPickup(Player player, IRod rod) {
    }
}
