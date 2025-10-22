package nl.devpieter.features.farming.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;

public class PlayerBlockBreakListener implements EventListener<@NotNull PlayerBlockBreakEvent> {

    @Override
    public @NotNull Class<PlayerBlockBreakEvent> eventType() {
        return PlayerBlockBreakEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerBlockBreakEvent event) {
        BlockHandler handler = event.getBlock().handler();
        if (handler == null) return Result.SUCCESS;

        if (!(handler instanceof FarmBlockHandler farmHandler)) return Result.SUCCESS;
        event.setCancelled(true);

        Instance instance = event.getInstance();
        Block block = event.getBlock();

        System.out.println("Player " + event.getPlayer().getUsername() + " broke block " + block.name() + " at " + event.getBlockPosition());

        BlockHolder holder = new BlockHolder(instance, event.getBlockPosition(), block);
        if (!farmHandler.isMature(holder)) return Result.SUCCESS;

        farmHandler.replant(holder, event.getPlayer());
        return Result.SUCCESS;
    }
}
