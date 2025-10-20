package nl.devpieter.features.farming.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import nl.devpieter.features.farming.blockHandlers.FarmCropHandler;
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

        if (!(handler instanceof FarmCropHandler cropHandler)) return Result.SUCCESS;
        event.setCancelled(true);

        Block block = event.getBlock();
        Instance instance = event.getInstance();

        String ageProperty = block.getProperty("age");
        if (ageProperty == null) return Result.SUCCESS;

        int age = Integer.parseInt(ageProperty);
        if (age < cropHandler.maxAge()) return Result.SUCCESS;

        cropHandler.replant(instance, block, event.getBlockPosition(), event.getPlayer());
        return Result.SUCCESS;
    }
}
