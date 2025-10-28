package nl.devpieter.features.fishing.listeners;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.bobber.IBobber;
import nl.devpieter.features.fishing.rod.IRod;
import nl.devpieter.utils.minestom.SoundUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PlayerUseItemListener implements EventListener<@NotNull PlayerUseItemEvent> {

    private static final HashMap<UUID, IBobber> bobbers = new HashMap<>();

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
            else {
                ItemStack item = event.getItemStack();
                IRod rod = FishingManager.getInstance().getRodByItem(item);
                if (rod != null) castFishingRod(player, rod);
            }
        }

        return Result.SUCCESS;
    }

    public static void castFishingRod(Player player, IRod rod) {
        IBobber bobber = rod.castBobber(player);
        bobbers.put(player.getUuid(), bobber);

        Sound sound = Sound.sound(
                SoundEvent.ENTITY_FISHING_BOBBER_THROW,
                Sound.Source.NEUTRAL,
                0.5f, 0.4f / (new Random().nextFloat() * 0.4f + 0.8f)
        );

        SoundUtils.playSoundForPlayer(player, sound);
        SoundUtils.playSoundToViewers(player, player.getPosition(), sound);
    }

    public static void reelInFishingRod(Player player) {
        IBobber bobber = bobbers.remove(player.getUuid());
        if (bobber == null) return;

        bobber.reelIn();
        bobbers.remove(player.getUuid());

        Sound sound = Sound.sound(
                SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE,
                Sound.Source.NEUTRAL,
                1.0f, 0.4f / (new Random().nextFloat() * 0.4f + 0.8f)
        );

        SoundUtils.playSoundForPlayer(player, sound);
        SoundUtils.playSoundToViewers(player, player.getPosition(), sound);
    }
}
