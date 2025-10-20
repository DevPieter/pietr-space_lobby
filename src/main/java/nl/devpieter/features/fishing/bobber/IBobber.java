package nl.devpieter.features.fishing.bobber;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.features.fishing.rod.IRod;
import org.jetbrains.annotations.Nullable;

public interface IBobber {

    IRod getRod();

    @Nullable ICatchable getCurrentCatchable();

    Player getThrower();

    void reelIn();

    EntityCreature getEntity();
}
