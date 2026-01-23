package nl.devpieter.features.parkour.models.parkourReplay;

import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FinishedPlayerReplay {

    private final UUID playerUuid;
    private final UUID replayUuid;

    private final Pos startPosition;
    private final List<PlayerReplayFrame> frames = new ArrayList<>();

    public FinishedPlayerReplay(UUID playerUuid, UUID replayUuid, PlayerReplay replay) {
        this.playerUuid = playerUuid;
        this.replayUuid = replayUuid;

        frames.addAll(replay.frames());
        startPosition = replay.startPosition();
    }

    public Pos startPosition() {
        return startPosition;
    }

    public List<PlayerReplayFrame> frames() {
        return frames;
    }
}
