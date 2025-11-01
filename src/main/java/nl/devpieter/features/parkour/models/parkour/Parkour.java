package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Parkour(
        String id,

        String worldId,
        Instance instance,

        @Nullable ParkourSpawnNode spawn,
        @Nullable ParkourLeaderboardNode leaderboard,

        ParkourStartNode start,
        ParkourEndNode end,

        List<ParkourNode> nodes
) {
    public Pos startPos() {
        return start.point().asPos()
                .add(0.5, 0, 0.5)
                .withYaw(start.yawPitch().yaw())
                .withPitch(start.yawPitch().pitch());
    }

    public BoundingBox startBox() {
        return start.boundingBox();
    }

    public Pos endPos() {
        return end.point().asPos()
                .add(0.5, 0, 0.5)
                .withYaw(end.yawPitch().yaw())
                .withPitch(end.yawPitch().pitch());
    }

    public BoundingBox endBox() {
        return end.boundingBox();
    }
}
