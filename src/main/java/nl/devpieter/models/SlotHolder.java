package nl.devpieter.models;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SlotHolder(int slot, @Nullable ItemStack itemStack) {

    public @NotNull ItemStack safeItemStack() {
        return itemStack != null ? itemStack : ItemStack.AIR;
    }
}
