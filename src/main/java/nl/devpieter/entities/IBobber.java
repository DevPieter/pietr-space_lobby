package nl.devpieter.entities;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import nl.devpieter.models.fishing.catchable.ICatchable;
import nl.devpieter.models.fishing.rod.IRod;
import org.jetbrains.annotations.Nullable;

public interface IBobber {

    IRod getRod();

    @Nullable ICatchable getCatchable();

    Player getThrower();

    void reelIn();

    EntityCreature getEntity();
}
