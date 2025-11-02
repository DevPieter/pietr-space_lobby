package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CampfireBlockHandler implements BlockHandler {

    class CampfireItem {
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

    private static final int MAX_SLOTS = 4;
    private final HashMap<Integer, CampfireItem> campfireItems = new HashMap<>();

    @Override
    public boolean onInteract(Interaction interaction) {
        Player player = interaction.getPlayer();

        BlockHolder campfireHolder = new BlockHolder(
                interaction.getInstance(),
                interaction.getBlockPosition(),
                interaction.getBlock()
        );

//        campfireItems.put(0, new CampfireItem(
//                ItemStack.of(Material.BEEF),
//                0,
//                600
//        ));
//
//        campfireItems.put(1, new CampfireItem(
//                ItemStack.of(Material.PORKCHOP),
//                0,
//                600
//        ));
//
//        campfireItems.put(2, new CampfireItem(
//                ItemStack.of(Material.CHICKEN),
//                0,
//                600
//        ));
//
//        campfireItems.put(3, new CampfireItem(
//                ItemStack.of(Material.MUTTON),
//                0,
//                600
//        ));

        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, Component.text("Campfire"));
        inventory.addItemStack(ItemStack.of(Material.BEEF));
        inventory.addItemStack(ItemStack.of(Material.PORKCHOP));
        inventory.addItemStack(ItemStack.of(Material.CHICKEN));
        inventory.addItemStack(ItemStack.of(Material.MUTTON));
        inventory.addItemStack(ItemStack.of(Material.POTATO));

        EventNode<@NotNull InventoryEvent> node = EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv)
                .addListener(InventoryPreClickEvent.class, event -> {
                    event.setCancelled(true);

                    ItemStack clickedItem = event.getClickedItem();
                    if (clickedItem.material() == Material.AIR) return;

                    if (campfireItems.size() >= MAX_SLOTS) {
                        event.getPlayer().sendMessage(Component.text("Campfire is full!"));
                        return;
                    }

                    int slot = 0;
                    while (campfireItems.containsKey(slot)) slot++;

                    campfireItems.put(slot, new CampfireItem(
                            clickedItem,
                            0,
                            600
                    ));

                    updateCampfire(campfireHolder);
                });

        MinecraftServer.getGlobalEventHandler().addChild(node);
        player.openInventory(inventory);
        return true;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "campfire");
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.NBT("Items"),
                Tag.NBT("CookingTimes"),
                Tag.NBT("CookingTotalTimes")
        );
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public void tick(Tick tick) {
        for (Map.Entry<Integer, CampfireItem> entry : campfireItems.entrySet()) {
            CampfireItem campfireItem = entry.getValue();

            if (campfireItem.cookingTime() < campfireItem.totalCookingTime()) {
                campfireItem.setCookingTime(campfireItem.cookingTime() + 1);
            } else {
                campfireItems.remove(entry.getKey());
            }
        }

        updateCampfire(new BlockHolder(
                tick.getInstance(),
                tick.getBlockPosition(),
                tick.getBlock()
        ));
    }

    private void updateCampfire(BlockHolder blockHolder) {
        List<BinaryTag> items = new ArrayList<>();

        int[] cookingTimesArray = new int[MAX_SLOTS];
        Arrays.fill(cookingTimesArray, 0);

        int[] totalCookingTimesArray = new int[MAX_SLOTS];
        Arrays.fill(totalCookingTimesArray, 0);

        for (Map.Entry<Integer, CampfireItem> entry : campfireItems.entrySet()) {
            int slot = entry.getKey();
            CampfireItem campfireItem = entry.getValue();

            items.add(CompoundBinaryTag.builder()
                    .putString("id", campfireItem.item().material().name().toLowerCase())
                    .putByte("Count", (byte) 1)
                    .putByte("Slot", (byte) slot)
                    .build()
            );

            cookingTimesArray[slot] = campfireItem.cookingTime();
            totalCookingTimesArray[slot] = campfireItem.totalCookingTime();
        }

        CompoundBinaryTag currentNbt = blockHolder.block().nbt();
        if (currentNbt == null) currentNbt = CompoundBinaryTag.empty();

        CompoundBinaryTag nbt = currentNbt
                .put("Items", ListBinaryTag.from(items))
                .putIntArray("CookingTimes", cookingTimesArray)
                .putIntArray("CookingTotalTimes", totalCookingTimesArray);

        Block updatedBlock = blockHolder.block().withNbt(nbt);
        blockHolder.instance().setBlock(blockHolder.position(), updatedBlock);
    }
}
