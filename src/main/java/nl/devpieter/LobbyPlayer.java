package nl.devpieter;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
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

        System.out.println("LobbyPlayer created for " + gameProfile.name());
    }

    public void switchToMainInventory() {
        switchToInventory(MAIN_INVENTORY);
    }

    public void switchToDynamicInventory() {
        switchToInventory(DYNAMIC_INVENTORY);
    }

    public void switchToCleanDynamicInventory() {
        switchToInventory(DYNAMIC_INVENTORY);
        inventory.clear();
    }

    public void fillDynamicInventory(SlotHolder... slots) {
        PlayerInventory dynamicInventory = inventories.get(DYNAMIC_INVENTORY);
        if (dynamicInventory == null) {
            dynamicInventory = new PlayerInventory();
            inventories.put(DYNAMIC_INVENTORY, dynamicInventory);
        }

        dynamicInventory.clear();

        for (SlotHolder slotHolder : slots) {
            dynamicInventory.setItemStack(slotHolder.slot(), slotHolder.itemStack());
        }
    }

    public boolean switchToInventory(@NotNull String key) {
        if (key.equals(currentInventoryKey)) return true;

        PlayerInventory inventory = inventories.get(key);
        if (inventory == null) {
            inventory = new PlayerInventory();
            inventories.put(key, inventory);
        }

        replaceInventory(inventory);
        currentInventoryKey = key;

        if (EnvStatics.DEBUG) {
            sendActionBar(TextUtils.miniText("<dark_gray>Inventory: <green>%s", key));
        }

        return true;
    }

    public String getCurrentInventoryKey() {
        return currentInventoryKey;
    }

    private void replaceInventory(PlayerInventory newInventory) {
        inventory.removeViewer(this);
        inventory = newInventory;
        inventory.addViewer(this);

        this.refreshActiveHand(true, false, false);
        this.sendPacketsToViewers(this.getEquipmentsPacket());
    }
}
