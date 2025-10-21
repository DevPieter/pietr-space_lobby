package nl.devpieter.features.fishing;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import nl.devpieter.features.fishing.catchable.CodCatchable;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.features.fishing.catchable.SalmonCatchable;
import nl.devpieter.features.fishing.rods.DefaultRod;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.features.fishing.rods.ProRod;
import nl.devpieter.features.fishing.rods.VanillaRod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FishingManager {

    private static FishingManager INSTANCE;

    private final List<IRod> rods = new ArrayList<>();
    private final List<ICatchable> catchables = new ArrayList<>();

    private FishingManager() {
        rods.addAll(List.of(
                new VanillaRod(),
                new DefaultRod(),
                new ProRod()
        ));

        catchables.addAll(List.of(
                new CodCatchable(),
                new SalmonCatchable()
        ));
    }

    public static FishingManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FishingManager();
        return INSTANCE;
    }

    public List<IRod> getRods() {
        return rods;
    }

    public List<ICatchable> getCatchables() {
        return catchables;
    }

    public @Nullable IRod getRodByItem(ItemStack itemStack) {
        for (IRod rod : this.rods) {
            UUID itemId = itemStack.getTag(Tag.UUID("rod_id"));
            if (itemId == null) continue;

            if (rod.getId().equals(itemId)) return rod;
        }
        return null;
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
