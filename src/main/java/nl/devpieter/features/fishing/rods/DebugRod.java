package nl.devpieter.features.fishing.rods;

import net.kyori.adventure.text.Component;
import nl.devpieter.features.fishing.models.BobberSettings;
import nl.devpieter.features.fishing.rod.Rod;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.UUID;

public class DebugRod extends Rod {

    @Override
    public UUID getId() {
        return UUID.fromString("b64e673d-fcb5-4e98-886f-f080b4999756");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<#646bc7>Debug Rod");
    }

    @Override
    public BobberSettings getBobberSettings() {
        return new BobberSettings(
                8 * 20, 8 * 20 + 1,
                2 * 20, 2 * 20 + 1,
                2 * 20, 2 * 20 + 1
        );
    }
}
