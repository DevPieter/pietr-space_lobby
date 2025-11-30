package nl.devpieter.features.parkour.models.parkourReplay;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityPose;

public record PlayerReplayFrame(
        Pos position,
        Vec velocity,
        boolean isSprinting,
        EntityPose entityPose
) {
}
