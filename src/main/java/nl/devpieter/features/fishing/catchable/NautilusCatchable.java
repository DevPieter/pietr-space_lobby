package nl.devpieter.features.fishing.catchable;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.rod.IRod;
import space.pietr.lobby.common.utils.minestom.TextUtils;

import java.util.UUID;

public class NautilusCatchable extends Catchable {

    @Override
    public UUID getId() {
        return UUID.fromString("29de8f0e-676b-41ee-a3bf-a90be929dd1f");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#d7d1c9:#977c68>Nautilus Shell");
    }

    @Override
    protected ItemStack getItemStack() {
        return ItemStack.of(Material.NAUTILUS_SHELL);
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
