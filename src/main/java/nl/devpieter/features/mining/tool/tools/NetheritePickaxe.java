package nl.devpieter.features.mining.tool.tools;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.mining.tool.Tool;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class NetheritePickaxe extends Tool {

    @Override
    protected ItemStack getBaseItem() {
        return ItemStack.of(Material.NETHERITE_PICKAXE);
    }

    @Override
    public UUID getId() {
        return UUID.fromString("dd8e0d67-9eba-4f75-9e65-0654549a3421");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("Netherite Pickaxe");
    }

    @Override
    public double getMiningStrength() {
        return 6;
    }

    @Override
    public double getMiningSpeedMultiplier() {
        return 1;
    }
}
