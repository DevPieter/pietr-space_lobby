package nl.devpieter.listeners;

import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import nl.devpieter.features.fishing.listeners.PlayerUseItemListener;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.parkour.ParkourReplayManager;
import nl.devpieter.features.parkour.enums.ParkourCancelReason;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.features.sitting.SitManager;
import nl.devpieter.features.tips.MsgManager;
import nl.devpieter.features.tips.TipManager;
import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.player.LobbyPlayer;

public class PlayerDisconnectListener implements EventListener<@NotNull PlayerDisconnectEvent> {

    private final MsgManager msgManager = MsgManager.getInstance();
    private final TipManager tipManager = TipManager.getInstance();
    private final SitManager sitManager = SitManager.getInstance();
    private final PackManager packManager = PackManager.getInstance();

    private final ParkourManager parkourManager = ParkourManager.getInstance();
    private final ParkourReplayManager replayManager = ParkourReplayManager.getInstance();

    @Override
    public @NotNull Class<PlayerDisconnectEvent> eventType() {
        return PlayerDisconnectEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerDisconnectEvent event) {
        if (!(event.getPlayer() instanceof LobbyPlayer player)) return Result.SUCCESS;

        msgManager.resetMessages(player);
        tipManager.resetTips(player);
        sitManager.standPlayer(player);
        packManager.clearStatus(player);

        parkourManager.cancelParkour(player, ParkourCancelReason.PlayerQuit);
        replayManager.clearReplays(player);

        PlayerUseItemListener.reelInFishingRod(player);
        return Result.SUCCESS;
    }
}
