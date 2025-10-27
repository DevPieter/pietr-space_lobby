package nl.devpieter.features.fishing.catchable;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.rod.IRod;

import java.util.UUID;

public interface ICatchable {

    UUID getId();

    Component getName();

    ItemStack buildItem();

    double getPickChance(Player player, IRod rod);

    void onLure(Player player, IRod rod, FishingBobber bobber);

    void onBite(Player player, IRod rod, FishingBobber bobber);

    void onCatch(Player player, IRod rod, FishingBobber bobber);

    void onFail(Player player, IRod rod, FishingBobber bobber);

    void onPickup(Player player, IRod rod);
}
