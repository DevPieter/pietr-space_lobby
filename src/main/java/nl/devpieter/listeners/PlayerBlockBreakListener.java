package nl.devpieter.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import nl.devpieter.blockHandlers.FarmCropHandler;
import org.jetbrains.annotations.NotNull;

public class PlayerBlockBreakListener implements EventListener<@NotNull PlayerBlockBreakEvent> {

    @Override
    public @NotNull Class<PlayerBlockBreakEvent> eventType() {
        return PlayerBlockBreakEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerBlockBreakEvent event) {
        event.setCancelled(true);

        BlockHandler handler = event.getBlock().handler();
        if (handler == null) return Result.SUCCESS;

        if (handler instanceof FarmCropHandler cropHandler) {
            Block block = event.getBlock();
            Instance instance = event.getInstance();

            String ageProperty = block.getProperty("age");
            if (ageProperty == null) return Result.SUCCESS;

            int age = Integer.parseInt(ageProperty);
            if (age < 7) return Result.SUCCESS;

            cropHandler.replant(instance, block, event.getBlockPosition(), event.getPlayer());
        }

        return Result.SUCCESS;
    }
}
