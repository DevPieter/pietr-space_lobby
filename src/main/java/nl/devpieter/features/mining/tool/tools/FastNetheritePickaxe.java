package nl.devpieter.features.mining.tool.tools;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.mining.tool.Tool;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class FastNetheritePickaxe extends Tool {

    @Override
    protected ItemStack getBaseItem() {
        return ItemStack.of(Material.NETHERITE_PICKAXE);
    }

    @Override
    public UUID getId() {
        return UUID.fromString("9df7e0d6-d546-4842-867d-7c2ac5836559");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("Fast Netherite Pickaxe");
    }

    @Override
    public double getMiningStrength() {
        return 6;
    }

    @Override
    public double getMiningSpeedMultiplier() {
        return 3;
    }
}
