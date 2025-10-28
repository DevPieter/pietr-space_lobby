package nl.devpieter.utils.minestom;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerUtils {

    public static void teleport(@NotNull Player player, Pos pos) {
        player.teleport(pos);
        player.setVelocity(new Vec(0));
    }

    public static boolean isInsideBlock(@NotNull Entity player, @NotNull Point blockPos) {
        return isInsideBlock(player, blockPos, 1.0, 1.0, 1.0);
    }

    public static boolean isInsideBlock(@NotNull Entity player, @NotNull Point blockPos, double blockHeight) {
        return isInsideBlock(player, blockPos, 1.0, blockHeight, 1.0);
    }

    public static boolean isInsideBlock(@NotNull Entity player, @NotNull Point blockPos, double blockWidth, double blockHeight, double blockDepth) {
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

        double blockMinX = blockPos.blockX();
        double blockMaxX = blockPos.blockX() + blockWidth;
        double blockMinY = blockPos.blockY();
        double blockMaxY = blockPos.blockY() + blockHeight;
        double blockMinZ = blockPos.blockZ();
        double blockMaxZ = blockPos.blockZ() + blockDepth;

        return (playerMinX < blockMaxX && playerMaxX > blockMinX) &&
                (playerMinY < blockMaxY && playerMaxY > blockMinY) &&
                (playerMinZ < blockMaxZ && playerMaxZ > blockMinZ);
    }
}
