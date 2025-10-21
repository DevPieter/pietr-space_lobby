package nl.devpieter.features.fishing.rods;

import net.kyori.adventure.text.Component;
import nl.devpieter.features.fishing.models.BobberSettings;
import nl.devpieter.features.fishing.rod.Rod;
import nl.devpieter.utils.TextUtils;

import java.util.UUID;

public class ProRod extends Rod {

    @Override
    public UUID getId() {
        return UUID.fromString("cac2542f-6b8d-441f-8c18-e21ef11dbb88");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("Pro Rod");
    }

    @Override
    public BobberSettings getBobberSettings() {
        return new BobberSettings(
                3 * 20, 6 * 20,
                2 * 20, 4 * 20,
                8 * 20, 20 * 20
        );
    }
}
