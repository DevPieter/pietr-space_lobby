package nl.devpieter.features.fishing;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import nl.devpieter.features.fishing.catchable.*;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.features.fishing.rods.DebugRod;
import nl.devpieter.features.fishing.rods.DefaultRod;
import nl.devpieter.features.fishing.rods.ProRod;
import nl.devpieter.features.fishing.rods.VanillaRod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FishingManager {

    private static FishingManager INSTANCE;

    private final List<IRod> rods = new ArrayList<>();
    private final List<ICatchable> catchables = new ArrayList<>();

    private final HashMap<UUID, FishingReferenceHolder> fishingReferences = new HashMap<>();

    private FishingManager() {
        rods.addAll(List.of(
                new VanillaRod(),
                new DebugRod(),
                new DefaultRod(),
                new ProRod()
        ));

        catchables.addAll(List.of(
                new CodCatchable(),
                new SalmonCatchable(),
                new NautilusCatchable(),
                new ShardCatchable()
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

    public void registerReference(Entity entity, FishingReferenceHolder reference) {
        fishingReferences.put(entity.getUuid(), reference);
    }

    public FishingReferenceHolder getReference(Entity entity) {
        return fishingReferences.get(entity.getUuid());
    }

    public void unregisterReference(Entity entity) {
        fishingReferences.remove(entity.getUuid());
    }

    public @Nullable IRod getRodByItem(ItemStack itemStack) {
        for (IRod rod : this.rods) {
            UUID itemId = itemStack.getTag(Tag.UUID("rod_id"));
            if (itemId == null) continue;

            if (rod.getId().equals(itemId)) return rod;
        }

        return null;
    }

    public @Nullable ICatchable getCatchableByItem(ItemStack itemStack) {
        for (ICatchable catchable : this.catchables) {
            UUID itemId = itemStack.getTag(Tag.UUID("catchable_id"));
            if (itemId == null) continue;

            if (catchable.getId().equals(itemId)) return catchable;
        }

        return null;
    }

    public @Nullable ICatchable getRandomCatchable(Player player, IRod rod) {
        if (this.catchables.isEmpty()) return null;

        int n = this.catchables.size();

        double[] weights = new double[n];
        double sum = 0.0d;

        for (int i = 0; i < n; i++) {
            double w = Math.max(0.0d, this.catchables.get(i).getPickChance(player, rod));
            weights[i] = w;
            sum += w;
        }

        // If every weight is zero, give equal chance
        if (sum == 0.0d) {
            Arrays.fill(weights, 1.0d);
            sum = n;
        }

        // If total is less than 100, distribute the remaining weight evenly across items
        if (sum < 100.0d) {
            double extraPerItem = (100.0d - sum) / n;
            for (int i = 0; i < n; i++) weights[i] += extraPerItem;

            sum = 100.0d;
        }

        double randomValue = Math.random() * sum;
        double cumulative = 0.0d;

        for (int i = 0; i < n; i++) {
            cumulative += weights[i];
            if (randomValue < cumulative) return this.catchables.get(i);
        }

        return null;
    }
}
