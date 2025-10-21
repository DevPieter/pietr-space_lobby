package nl.devpieter.features.fishing.rod;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.fishing.models.BobberSettings;

import java.util.UUID;

public class DefaultRod extends Rod {

    @Override
    public UUID getId() {
        return UUID.fromString("67f79f88-1bed-456c-81bf-67c0a816d251");
    }

    @Override
    public Component getName() {
        return Component.text("Default Rod");
    }

    @Override
    public BobberSettings getBobberSettings() {
        return new BobberSettings(
                20, 2 * 20,
                20, 4 * 20,
                5 * 20, 30 * 20
        );
    }
}
