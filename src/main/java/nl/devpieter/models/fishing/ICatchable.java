package nl.devpieter.models.fishing;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public interface ICatchable {

    Component getName();

    ItemStack getItem();

    double getCatchChance(Player player, ItemStack rod);

    void onLure(Player player, ItemStack rod);

    void onBite(Player player, ItemStack rod);

    void onCatch(Player player, ItemStack rod);

    void onFail(Player player, ItemStack rod);
}
