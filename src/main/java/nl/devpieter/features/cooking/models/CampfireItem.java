package nl.devpieter.features.cooking.models;

import net.minestom.server.item.ItemStack;

public class CampfireItem {

    private final ItemStack item;
    private final int totalCookingTime;

    private int cookingTime;

    public CampfireItem(ItemStack item, int cookingTime, int totalCookingTime) {
        this.item = item;
        this.totalCookingTime = totalCookingTime;

        this.cookingTime = cookingTime;
    }

    public ItemStack item() {
        return item;
    }

    public int cookingTime() {
        return cookingTime;
    }

    public int totalCookingTime() {
        return totalCookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }
}
