package nl.devpieter.features.fishing.rods;

import net.kyori.adventure.text.Component;
import nl.devpieter.features.fishing.models.BobberSettings;
import nl.devpieter.features.fishing.rod.Rod;
import space.pietr.lobby.common.utils.minestom.TextUtils;

import java.util.UUID;

public class DefaultRod extends Rod {

    @Override
    public UUID getId() {
        return UUID.fromString("67f79f88-1bed-456c-81bf-67c0a816d251");
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<#767aab>Default Rod");
    }

    @Override
    public BobberSettings getBobberSettings() {
        return new BobberSettings(
                2 * 20, 4 * 20,
                3 * 20, 5 * 20,
                10 * 20, 25 * 20
        );
    }
}
