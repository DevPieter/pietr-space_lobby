package nl.devpieter.features.parkour.models.parkourReplay;

import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;

public class PlayerReplay {

    private final Pos startPosition;
    private final List<PlayerReplayFrame> frames = new ArrayList<>();

    private long startTime;
    private long endTime;

    private boolean tracking = false;

    public PlayerReplay(Pos startPosition) {
        this.startPosition = startPosition;
    }

    public void startTracking() {
        frames.clear();

        startTime = System.currentTimeMillis();
        endTime = 0;

        tracking = true;
    }

    public void stopTracking() {
        endTime = System.currentTimeMillis();
        tracking = false;
    }

    public void pushFrame(PlayerReplayFrame frame) {
        if (!tracking) return;
        frames.add(frame);
    }

    public Pos startPosition() {
        return startPosition;
    }

    public List<PlayerReplayFrame> frames() {
        return frames;
    }
}
