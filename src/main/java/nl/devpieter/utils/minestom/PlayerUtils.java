package nl.devpieter.utils.minestom;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerUtils {

    public static void teleport(@NotNull Player player, Pos pos) {
        player.teleport(pos);
        player.setVelocity(new Vec(0));
    }

    public static boolean isInsideBox(@NotNull Player player, @NotNull Point origin, @NotNull BoundingBox box) {
        BoundingBox playerBB = player.getBoundingBox();
        Pos playerPos = player.getPosition();

        double halfWidth = playerBB.width() / 2;
        double height = playerBB.height();

        double playerMinX = playerPos.x() - halfWidth;
        double playerMaxX = playerPos.x() + halfWidth;
        double playerMinY = playerPos.y();
        double playerMaxY = playerPos.y() + height;
        double playerMinZ = playerPos.z() - halfWidth;
        double playerMaxZ = playerPos.z() + halfWidth;

        Vec relativeStart = box.relativeStart();
        Vec relativeEnd = box.relativeEnd();

        double boxMinX = origin.x() + Math.min(relativeStart.x(), relativeEnd.x());
        double boxMaxX = origin.x() + Math.max(relativeStart.x(), relativeEnd.x());

        double boxMinY = origin.y() + Math.min(relativeStart.y(), relativeEnd.y());
        double boxMaxY = origin.y() + Math.max(relativeStart.y(), relativeEnd.y());

        double boxMinZ = origin.z() + Math.min(relativeStart.z(), relativeEnd.z());
        double boxMaxZ = origin.z() + Math.max(relativeStart.z(), relativeEnd.z());

        return (playerMinX < boxMaxX && playerMaxX > boxMinX) &&
                (playerMinY < boxMaxY && playerMaxY > boxMinY) &&
                (playerMinZ < boxMaxZ && playerMaxZ > boxMinZ);
    }
}
