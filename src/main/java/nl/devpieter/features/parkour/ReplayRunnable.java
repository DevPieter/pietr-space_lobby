package nl.devpieter.features.parkour;

import net.minestom.server.entity.Player;
import nl.devpieter.entities.Mannequin;
import nl.devpieter.features.parkour.models.replay.FinishedPlayerReplay;
import nl.devpieter.features.parkour.models.replay.PlayerReplayFrame;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ReplayRunnable implements Runnable {

    private final AtomicReference<Integer> index = new AtomicReference<>(0);

    private final Mannequin mannequin;
    private final FinishedPlayerReplay replay;

    private final Consumer<Void> onFinish;

    public ReplayRunnable(Player player, FinishedPlayerReplay replay, Consumer<Void> onFinish) {
        this.mannequin = new Mannequin(player);
        this.replay = replay;
        this.onFinish = onFinish;

        mannequin.setInstance(player.getInstance(), replay.startPosition());
    }

    @Override
    public void run() {
        int i = index.get();
        List<PlayerReplayFrame> frames = replay.frames();

        if (i >= frames.size()) {
            mannequin.remove();
            onFinish.accept(null);
            return;
        }

        PlayerReplayFrame frame = frames.get(i);
        mannequin.refreshPosition(frame.position(), false);
        mannequin.teleport(frame.position(), frame.velocity());
        mannequin.setVelocity(frame.velocity());
        mannequin.setSprinting(frame.isSprinting());
        mannequin.setPose(frame.entityPose());

        index.updateAndGet(v -> v + 1);
    }

    public void cancel() {
        mannequin.remove();
    }
}
