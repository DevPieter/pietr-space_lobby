package nl.devpieter.features.mining.tools;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.mining.tool.Tool;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class IronPickaxe extends Tool {

    @Override
    protected ItemStack getBaseItem() {
        return ItemStack.of(Material.IRON_PICKAXE);
    }

    @Override
    public UUID getId() {
        return UUID.fromString("ab483f28-aa2d-44a3-9b8e-80a64e250706");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("Iron Pickaxe");
    }

    @Override
    public double getMiningStrength() {
        return 2;
    }

    @Override
    public double getMiningSpeedMultiplier() {
        return 1;
    }
}
