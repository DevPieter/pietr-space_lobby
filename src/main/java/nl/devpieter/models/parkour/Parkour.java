package nl.devpieter.models.parkour;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Parkour {
    private final UUID id;
    private final UUID worldId;

    private final String name;
    private final Pos start;
    private final Pos end;

    private final Pos leaderboardPos;

    private final List<ParkourNode> nodes;
    private final Map<BlockVec, ParkourNode> nodeMap;

    public Parkour(UUID id, UUID worldId, String name, Pos start, Pos end, Pos leaderboardPos, List<ParkourNode> nodes) {
        this.id = id;
        this.worldId = worldId;
        this.name = name;
        this.start = start;
        this.end = end;
        this.leaderboardPos = leaderboardPos;
        this.nodes = nodes;

        this.nodeMap = nodes.stream().collect(Collectors.toMap(parkourNode -> new BlockVec(parkourNode.position()), parkourNode -> parkourNode));
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
        return start;
    }

    public Pos end() {
        return end;
    }

    public Pos leaderboardPos() {
        return leaderboardPos;
    }

    public List<ParkourNode> nodes() {
        return nodes;
    }

    public Map<BlockVec, ParkourNode> nodeMap() {
        return nodeMap;
    }
}
