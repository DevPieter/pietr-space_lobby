package nl.devpieter.features.fishing.rod;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import nl.devpieter.features.fishing.bobber.IBobber;
import nl.devpieter.features.fishing.models.BobberSettings;

import java.util.UUID;

public interface IRod {

    UUID getId();

    Component getName();

    ItemStack buildItem();

    BobberSettings getBobberSettings();

    IBobber castBobber(Player player);
}
