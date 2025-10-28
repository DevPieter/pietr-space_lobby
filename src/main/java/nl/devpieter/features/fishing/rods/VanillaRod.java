package nl.devpieter.features.fishing.rods;

import net.kyori.adventure.text.Component;
import nl.devpieter.features.fishing.models.BobberSettings;
import nl.devpieter.features.fishing.rod.Rod;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class VanillaRod extends Rod {

    @Override
    public UUID getId() {
        return UUID.fromString("f1c6a5e8-ae54-4cae-aea9-b5c0f014cae7");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("Fishing Rod");
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
