package nl.devpieter.entities;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import nl.devpieter.TagHelper;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.enums.PlayerTags;
import nl.devpieter.models.SlotHolder;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LobbyPlayer extends Player {

    public static final String MAIN_INVENTORY = "main";
    public static final String DYNAMIC_INVENTORY = "__dynamic__";

    private final List<String> activeTags = new ArrayList<>();
    private final HashMap<String, PlayerInventory> inventories = new HashMap<>();

    private String currentInventoryKey = MAIN_INVENTORY;

    public LobbyPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        inventories.put(MAIN_INVENTORY, getInventory());
    }

    public boolean hasTag(@NotNull PlayerTags tag) {
        return hasTag(tag.tag());
    }

    public boolean hasAnyTag(@NotNull PlayerTags... tags) {
        for (PlayerTags tag : tags) {
            if (hasTag(tag)) return true;
        }

        return false;
    }

    public boolean hasTag(@NotNull String tag) {
        return activeTags.contains(tag);
    }

    public boolean addTag(@NotNull PlayerTags tag) {
        return addTag(tag.tag());
    }

    public boolean addTag(@NotNull String tag) {
        if (hasTag(tag)) return false;
        return activeTags.add(tag);
    }

    public boolean removeTag(@NotNull PlayerTags tag) {
        return removeTag(tag.tag());
    }

    public boolean removeTag(@NotNull String tag) {
        return activeTags.remove(tag);
    }

    public boolean isMainInventoryActive() {
        return currentInventoryKey.equals(MAIN_INVENTORY);
    }

    public void switchToMainInventory() {
        switchToInventory(MAIN_INVENTORY);
    }

    public boolean isDynamicInventoryActive() {
        return currentInventoryKey.equals(DYNAMIC_INVENTORY);
    }

    public void switchToDynamicInventory() {
        switchToInventory(DYNAMIC_INVENTORY);
    }

    public void addItemToMainInventory(@NotNull ItemStack itemStack) {
        PlayerInventory mainInventory = getOrCreateInventory(MAIN_INVENTORY);
        mainInventory.addItemStack(itemStack);
    }

    public void addItemToMainInventory(@NotNull ItemStack itemStack, int slot) {
        PlayerInventory mainInventory = getOrCreateInventory(MAIN_INVENTORY);
        mainInventory.setItemStack(slot, itemStack);
    }

    public void fillDynamicInventory(SlotHolder @NotNull ... slots) {
        PlayerInventory dynamicInventory = getOrCreateInventory(DYNAMIC_INVENTORY);
        dynamicInventory.clear();

        for (SlotHolder slotHolder : slots) {
            dynamicInventory.setItemStack(slotHolder.slot(), slotHolder.safeItemStack());
        }
    }

    public void switchToInventory(@NotNull String key) {
        if (key.equals(currentInventoryKey)) return;

        replaceInventory(getOrCreateInventory(key));
        currentInventoryKey = key;

        if (EnvStatics.DEBUG) {
            sendActionBar(TextUtils.miniText("<dark_gray>Inventory: <green>%s", key));
        }
    }

    public String getCurrentInventoryKey() {
        return currentInventoryKey;
    }

    private @NotNull PlayerInventory getOrCreateInventory(@NotNull String key) {
        PlayerInventory inventory = inventories.get(key);
        if (inventory != null) return inventory;

        inventory = new PlayerInventory();
        inventories.put(key, inventory);
        return inventory;
    }

    private void replaceInventory(PlayerInventory newInventory) {
        inventory.removeViewer(this);
        inventory = newInventory;
        inventory.addViewer(this);

        refreshActiveHand(true, false, false);
        sendPacketToViewersAndSelf(getEquipmentsPacket());

        inventory.update();
    }

    @Override
    public void leaveBed() {
        removeTag(PlayerTags.LAYING);
        super.leaveBed();

        System.out.println("Player " + getUsername() + " has left the bed and is no longer laying.");
    }

    @Override
    public void enterBed(Point point) {
        if (!TagHelper.canLayPlayer(this)) return;

        addTag(PlayerTags.LAYING);
        super.enterBed(point);

        System.out.println("Player " + getUsername() + " has entered the bed and is now laying.");
    }
}
