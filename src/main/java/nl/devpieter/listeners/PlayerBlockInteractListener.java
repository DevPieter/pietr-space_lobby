package nl.devpieter.listeners;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import nl.devpieter.managers.SitManager;
import org.jetbrains.annotations.NotNull;

public class PlayerBlockInteractListener implements EventListener<@NotNull PlayerBlockInteractEvent> {

    private final SitManager sitManager = SitManager.getInstance();

    @Override
    public @NotNull Class<PlayerBlockInteractEvent> eventType() {
        return PlayerBlockInteractEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerBlockInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != PlayerHand.MAIN || !player.isSneaking()) return Result.SUCCESS;

        BlockVec block = event.getBlockPosition();
        Block blockAbove = player.getInstance().getBlock(block.asVec().add(0, 1, 0));
        if (!blockAbove.isAir()) return Result.SUCCESS;

        this.sitManager.sitPlayer(player, event.getBlock(), block);
        return Result.SUCCESS;
    }
}
