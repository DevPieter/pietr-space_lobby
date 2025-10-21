package nl.devpieter.features.fishing.rod;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.models.BobberSettings;

import java.util.UUID;

public class ProRod extends Rod {

    @Override
    public UUID getId() {
        return UUID.fromString("cac2542f-6b8d-441f-8c18-e21ef11dbb88");
    }

    @Override
    public Component getName() {
        return Component.text("Pro Rod");
    }

    @Override
    public BobberSettings getBobberSettings() {
        return new BobberSettings(
                2 * 20, 3 * 20,
                20, 3 * 20,
                5 * 20, 20 * 20
        );
    }
}
