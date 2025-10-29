package nl.devpieter;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.models.SlotHolder;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class LobbyPlayer extends Player {

    public static final String MAIN_INVENTORY = "main";
    public static final String DYNAMIC_INVENTORY = "__dynamic__";

    private final HashMap<String, PlayerInventory> inventories = new HashMap<>();

    private String currentInventoryKey = MAIN_INVENTORY;

    public LobbyPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        inventories.put(MAIN_INVENTORY, getInventory());
    }

    public void switchToMainInventory() {
        switchToInventory(MAIN_INVENTORY);
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

        this.refreshActiveHand(true, false, false);
        this.sendPacketsToViewers(this.getEquipmentsPacket());
    }
}
