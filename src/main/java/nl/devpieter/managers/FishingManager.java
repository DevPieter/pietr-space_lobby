package nl.devpieter.managers;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import nl.devpieter.models.fishing.ICatchable;
import nl.devpieter.models.fishing.SalmonCatchable;

import java.util.ArrayList;
import java.util.List;

public class FishingManager {

    private static FishingManager INSTANCE;

    private final List<ICatchable> catchables = new ArrayList<>();

    private FishingManager() {
        catchables.add(new SalmonCatchable());
    }

    public static FishingManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FishingManager();
        return INSTANCE;
    }

    public List<ICatchable> getCatchables() {
        return catchables;
    }

    public ICatchable getRandomCatchable(Player player, ItemStack rod) {
        return catchables.get(0);
    }
}
