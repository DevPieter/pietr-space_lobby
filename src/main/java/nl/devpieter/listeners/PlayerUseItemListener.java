package nl.devpieter.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import nl.devpieter.FishingBobber;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class PlayerUseItemListener implements EventListener<@NotNull PlayerUseItemEvent> {

    private final HashMap<UUID, FishingBobber> bobbers = new HashMap<>();

    @Override
    public @NotNull Class<PlayerUseItemEvent> eventType() {
        return PlayerUseItemEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerUseItemEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != PlayerHand.MAIN) return Result.SUCCESS;

        if (event.getItemStack().material() == Material.FISHING_ROD) {
            if (bobbers.containsKey(player.getUuid())) reelInFishingRod(player);
            else castFishingRod(player);
        }

        return Result.SUCCESS;
    }

    private void castFishingRod(Player player) {
        FishingBobber bobber = new FishingBobber(player, player.getInstance());
        bobber.setInstance(player.getInstance());

        bobbers.put(player.getUuid(), bobber);
    }

    private void reelInFishingRod(Player player) {
        FishingBobber bobber = bobbers.remove(player.getUuid());
        if (bobber == null) return;

        bobber.reelIn();
        bobbers.remove(player.getUuid());
    }
}
