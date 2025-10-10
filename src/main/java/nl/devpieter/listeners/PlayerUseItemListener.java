package nl.devpieter.listeners;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import net.minestom.server.utils.MathUtils;
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
        Pos eye = player.getPosition().withY(player.getPosition().y() + player.getEyeHeight());
        Vec dir = player.getPosition().direction();

        FishingBobber bobber = new FishingBobber(player, player.getInstance(), eye, dir.mul(5));
        bobbers.put(player.getUuid(), bobber);
    }

    private void reelInFishingRod(Player player) {
        FishingBobber bobber = bobbers.remove(player.getUuid());
        if (bobber != null) bobber.remove();
    }
}
