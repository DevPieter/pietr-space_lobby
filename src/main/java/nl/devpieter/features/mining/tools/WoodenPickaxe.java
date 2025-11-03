package nl.devpieter.features.mining.tools;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.mining.tool.Tool;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class WoodenPickaxe extends Tool {

    @Override
    protected ItemStack getBaseItem() {
        return ItemStack.of(Material.WOODEN_PICKAXE);
    }

    @Override
    public UUID getId() {
        return UUID.fromString("3edded65-0f46-42aa-bad1-4c26c39aefba");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("Wooden Pickaxe");
    }

    @Override
    public double getMiningStrength() {
        return 1;
    }

    @Override
    public double getMiningSpeedMultiplier() {
        return 1;
    }
}
