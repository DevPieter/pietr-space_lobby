package nl.devpieter.features.cooking.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
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
import nl.devpieter.features.cooking.models.CampfireItem;
import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.models.holder.block.IBlockHolder;

import java.util.*;

public class CampfireBlockHandler implements BlockHandler {

    private static final int MAX_SLOTS = 4;
    private final HashMap<Integer, CampfireItem> campfireItems = new HashMap<>();

    private long lastInventoryClickTime = 0;

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
    public boolean onInteract(Interaction interaction) {
        if (interaction.getHand() != PlayerHand.MAIN) return false;

        Player player = interaction.getPlayer();

        String lit = interaction.getBlock().getProperty("lit");
        if (lit == null || lit.equals("false")) return false;

        IBlockHolder campfireHolder = new IBlockHolder(
                interaction.getInstance(),
                interaction.getBlockPosition(),
                interaction.getBlock()
        );

        Inventory inventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("Campfire"));
        inventory.addItemStack(ItemStack.of(Material.BEEF));
        inventory.addItemStack(ItemStack.of(Material.PORKCHOP));
        inventory.addItemStack(ItemStack.of(Material.CHICKEN));
        inventory.addItemStack(ItemStack.of(Material.MUTTON));
        inventory.addItemStack(ItemStack.of(Material.RABBIT));
        inventory.addItemStack(ItemStack.of(Material.COD));
        inventory.addItemStack(ItemStack.of(Material.SALMON));
        inventory.addItemStack(ItemStack.of(Material.TROPICAL_FISH));
        inventory.addItemStack(ItemStack.of(Material.PUFFERFISH));
        inventory.addItemStack(ItemStack.of(Material.POTATO));
        inventory.addItemStack(ItemStack.of(Material.CARROT));
        inventory.addItemStack(ItemStack.of(Material.BEETROOT));

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItemStack(slot).material() == Material.AIR) {
                inventory.setItemStack(slot, ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).with(builder -> {
                    builder.set(DataComponents.CUSTOM_NAME, Component.text(""));
                }));
            }
        }

        EventNode<@NotNull InventoryEvent> node = EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv)
                .addListener(InventoryPreClickEvent.class, event -> {
                    event.setCancelled(true);

                    if (System.currentTimeMillis() - lastInventoryClickTime < 200) return;
                    lastInventoryClickTime = System.currentTimeMillis();

                    ItemStack clickedItem = event.getClickedItem();
                    if (clickedItem.material() == Material.AIR || clickedItem.material() == Material.GRAY_STAINED_GLASS_PANE) {
                        return;
                    }

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
    public void tick(Tick tick) {
        String lit = tick.getBlock().getProperty("lit");
        if (lit == null || lit.equals("false")) return;

        Iterator<Map.Entry<Integer, CampfireItem>> iterator = campfireItems.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, CampfireItem> entry = iterator.next();
            CampfireItem campfireItem = entry.getValue();

            if (campfireItem.cookingTime() < campfireItem.totalCookingTime()) {
                campfireItem.setCookingTime(campfireItem.cookingTime() + 1);
            } else {
                iterator.remove();
            }
        }

        updateCampfire(new IBlockHolder(
                tick.getInstance(),
                tick.getBlockPosition(),
                tick.getBlock()
        ));
    }

    private void updateCampfire(IBlockHolder blockHolder) {
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
