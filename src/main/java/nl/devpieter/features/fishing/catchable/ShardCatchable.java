package nl.devpieter.features.fishing.catchable;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class ShardCatchable extends Catchable {

    @Override
    public UUID getId() {
        return UUID.fromString("81c32d65-efad-43fa-bb71-901399e0bb09");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#aed4c8:#68a390>Prismarine Shard");
    }

    @Override
    protected ItemStack getItemStack() {
        return ItemStack.of(Material.PRISMARINE_SHARD);
    }

    @Override
    public double getPickChance(Player player, IRod rod) {
        return 10.0;
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
