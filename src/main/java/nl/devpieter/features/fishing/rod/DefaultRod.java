package nl.devpieter.features.fishing.rod;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.models.BobberSettings;

public class DefaultRod implements IRod {

    @Override
    public Component getName() {
        return Component.text("Fishing Rod");
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.of(Material.FISHING_ROD).withCustomName(getName());
    }

    @Override
    public BobberSettings getBobberSettings() {
//        return new BobberSettings(
//                20, 40,
//                20, 80,
//                100, 600
//        );

        // TODO - For testing purposes only
        return new BobberSettings(
                40, 80,
                20, 40,
                20, 40
        );
    }

    @Override
    public void onUse() {

    }
}
