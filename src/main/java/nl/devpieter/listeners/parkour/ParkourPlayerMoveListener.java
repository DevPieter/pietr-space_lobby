package nl.devpieter.listeners.parkour;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.managers.ParkourManager;
import nl.devpieter.models.Parkour;
import org.jetbrains.annotations.NotNull;

public class ParkourPlayerMoveListener implements EventListener<@NotNull PlayerMoveEvent> {

    private final ParkourManager parkourManager = ParkourManager.getInstance();

    @Override
    public @NotNull Class<PlayerMoveEvent> eventType() {
        return PlayerMoveEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerMoveEvent event) {
        Pos pos = event.getNewPosition();
//        if (event.getPlayer().getPosition().sameView(pos)) return Result.SUCCESS;

        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        if (instance == null) return Result.SUCCESS;

//        Block block = instance.getBlock(pos);

//        if (block == Block.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Parkour parkour = this.parkourManager.getParkour(instance.getUuid(), pos);
            if (parkour != null) this.parkourManager.startParkour(player, parkour.uuid());;
//        } else if (block == Block.SPRUCE_PRESSURE_PLATE) {
            this.parkourManager.checkCheckpoint(player, pos);
//        } else if (block == Block.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            this.parkourManager.checkFinish(player, pos);
//        }

        return Result.SUCCESS;
    }
}
