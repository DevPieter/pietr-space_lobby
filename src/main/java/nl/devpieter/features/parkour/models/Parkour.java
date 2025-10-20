package nl.devpieter.features.parkour.models;

import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.UUID;

public class Parkour {
    private final UUID id;
    private final UUID worldId;

    private final String name;
    private final Pos startPosition;
    private final Pos endPosition;

    private final Pos leaderboardPosition;

    private final List<ParkourNode> nodes;

    public Parkour(UUID id, UUID worldId, String name, Pos startPosition, Pos endPosition, Pos leaderboardPosition, List<ParkourNode> nodes) {
        this.id = id;
        this.worldId = worldId;
        this.name = name;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.leaderboardPosition = leaderboardPosition;
        this.nodes = nodes;
    }

    public UUID id() {
        return id;
    }

    public UUID worldId() {
        return worldId;
    }

    public String name() {
        return name;
    }

    public Pos start() {
        return startPosition;
    }

    public Pos end() {
        return endPosition;
    }

    public Pos leaderboardPos() {
        return leaderboardPosition;
    }

    public List<ParkourNode> nodes() {
        return nodes;
    }
}
