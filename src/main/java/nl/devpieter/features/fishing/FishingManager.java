package nl.devpieter.features.fishing;

import net.minestom.server.entity.Player;
import nl.devpieter.features.fishing.catchable.CodCatchable;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.features.fishing.catchable.SalmonCatchable;
import nl.devpieter.features.fishing.rod.IRod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FishingManager {

    private static FishingManager INSTANCE;

    private final List<ICatchable> catchables = new ArrayList<>();

    private FishingManager() {
        catchables.add(new CodCatchable());
        catchables.add(new SalmonCatchable());
    }

    public static FishingManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FishingManager();
        return INSTANCE;
    }

    public List<ICatchable> getCatchables() {
        return catchables;
    }

    public @Nullable ICatchable getRandomCatchable(Player player, IRod rod) {
        if (this.catchables.isEmpty()) return null;

        double totalWeight = Math.max(100.0d, this.catchables.stream().mapToDouble(c -> c.getPickChance(player, rod)).sum());
        double randomValue = Math.random() * totalWeight;
        double cumulativeWeight = 0.0d;

        for (ICatchable catchable : this.catchables) {
            cumulativeWeight += catchable.getPickChance(player, rod);
            if (randomValue < cumulativeWeight) return catchable;
        }

        return null;
    }
}
