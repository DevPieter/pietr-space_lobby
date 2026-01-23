package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Parkour/*(
        String id,

        String worldId,
        Instance instance,

        @Nullable ParkourSpawnNode spawnNode,
        @Nullable ParkourLeaderboardNode leaderboardNode,

        ParkourStartNode startNode,
        ParkourEndNode endNode,

        List<ParkourNode> nodes
)*/ {

    private final String id;

    private final String worldId;
    private final Instance instance;

    private final @Nullable ParkourSpawnNode spawnNode;
    private final @Nullable ParkourLeaderboardNode leaderboardNode;

    private final ParkourStartNode startNode;
    private final ParkourEndNode endNode;

    private final List<ParkourNode> nodes;

    private Acquirable<@NotNull ? extends Entity> leaderboardEntityAcquirable;
//    private Entity leaderboardEntity;

    public Parkour(String id, String worldId, Instance instance, @Nullable ParkourSpawnNode spawnNode, @Nullable ParkourLeaderboardNode leaderboardNode, ParkourStartNode startNode, ParkourEndNode endNode, List<ParkourNode> nodes) {
        this.id = id;

        this.worldId = worldId;
        this.instance = instance;

        this.spawnNode = spawnNode;
        this.leaderboardNode = leaderboardNode;

        this.startNode = startNode;
        this.endNode = endNode;

        this.nodes = nodes;
    }

    public String id() {
        return id;
    }

    public String worldId() {
        return worldId;
    }

    public Instance instance() {
        return instance;
    }

    public @Nullable ParkourSpawnNode spawnNode() {
        return spawnNode;
    }

    public @Nullable ParkourLeaderboardNode leaderboardNode() {
        return leaderboardNode;
    }

    public ParkourStartNode startNode() {
        return startNode;
    }

    public ParkourEndNode endNode() {
        return endNode;
    }

    public List<ParkourNode> nodes() {
        return nodes;
    }

    public @Nullable Acquirable<@NotNull ? extends Entity> leaderboardEntity() {
        return leaderboardEntityAcquirable;
    }

    public void setLeaderboardEntity(Acquirable<@NotNull ? extends Entity> leaderboardEntity) {
        this.leaderboardEntityAcquirable = leaderboardEntity;
    }

//    public Entity leaderboardEntity() {
//        return leaderboardEntity;
//    }
//
//    public void setLeaderboardEntity(Entity leaderboardEntity) {
//        this.leaderboardEntity = leaderboardEntity;
//    }

    public @Nullable Pos spawnPos() {
        if (spawnNode == null) return null;

        return spawnNode.point().asPos()
                .add(0.5, 0, 0.5)
                .withYaw(spawnNode.yawPitch().yaw())
                .withPitch(spawnNode.yawPitch().pitch());
    }

    public Pos startPos() {
        return startNode.point().asPos()
                .add(0.5, 0, 0.5)
                .withYaw(startNode.yawPitch().yaw())
                .withPitch(startNode.yawPitch().pitch());
    }

    public BoundingBox startBox() {
        return startNode.boundingBox();
    }

    public Pos endPos() {
        return endNode.point().asPos()
                .add(0.5, 0, 0.5)
                .withYaw(endNode.yawPitch().yaw())
                .withPitch(endNode.yawPitch().pitch());
    }

    public BoundingBox endBox() {
        return endNode.boundingBox();
    }
}
