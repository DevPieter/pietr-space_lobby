package nl.devpieter.features.fishing;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.features.fishing.rod.IRod;

public record FishingReferenceHolder(
        Player player,
        Entity entity,
        IRod rod,
        ICatchable catchable
) {
}
