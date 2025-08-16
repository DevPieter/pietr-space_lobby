package nl.devpieter.models.parkour;

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
//    private final Map<BlockVec, ParkourNode> nodeMap;

    public Parkour(UUID id, UUID worldId, String name, Pos startPosition, Pos endPosition, Pos leaderboardPosition, List<ParkourNode> nodes) {
        this.id = id;
        this.worldId = worldId;
        this.name = name;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.leaderboardPosition = leaderboardPosition;
        this.nodes = nodes;

//        this.nodeMap = nodes.stream().collect(Collectors.toMap(parkourNode -> new BlockVec(parkourNode.position()), parkourNode -> parkourNode));
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

//    public Map<BlockVec, ParkourNode> nodeMap() {
//        return nodeMap;
//    }
}
