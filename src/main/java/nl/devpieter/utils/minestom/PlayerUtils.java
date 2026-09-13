package nl.devpieter.utils.minestom;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import nl.devpieter.features.world.models.LoadedWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerUtils {

    public static void teleport(@NotNull Player player, Pos pos) {
        player.teleport(pos);
        player.setVelocity(new Vec(0));
    }

    public static void moveWorld(@NotNull Player player, @NotNull LoadedWorld world) {
        player.setInstance(world.instance(), world.world().settings().spawn().toPoint());
        player.setVelocity(new Vec(0));
    }

    public static <T> @Nullable T readPlayerTag(@NotNull Player player, Tag<T> tag) {
        return player.getTag(tag);
    }

    public static <T> boolean writePlayerTag(@NotNull Player player, Tag<T> tag, T value) {
        if (player.getTag(tag) != null) return false;
        player.setTag(tag, value);

        return true;
    }

    public static <T> boolean removePlayerTag(@NotNull Player player, Tag<T> tag) {
        if (player.getTag(tag) == null) return false;
        player.removeTag(tag);

        return true;
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

    public static boolean hasLowHeadroom(Player player) {
        Block above = player.getInstance().getBlock(player.getPosition().add(0, 1, 0));
        return !above.air();
    }

    public static boolean isPlayerInWater(Player player) {
        BoundingBox box = player.getBoundingBox();
        Instance instance = player.getInstance();

        Pos playerPos = player.getPosition();
        double halfWidth = box.width() / 2;
        double height = box.height();

        double minX = playerPos.x() - halfWidth;
        double maxX = playerPos.x() + halfWidth;
        double minY = playerPos.y();
        double maxY = playerPos.y() + height;
        double minZ = playerPos.z() - halfWidth;
        double maxZ = playerPos.z() + halfWidth;

        double[] xPoints = new double[]{minX, (minX + maxX) / 2, maxX};
        double[] yPoints = new double[]{minY, (minY + maxY) / 2, maxY};
        double[] zPoints = new double[]{minZ, (minZ + maxZ) / 2, maxZ};

        for (double x : xPoints) {
            for (double y : yPoints) {
                for (double z : zPoints) {

                    Block block = instance.getBlock(
                            (int) Math.floor(x),
                            (int) Math.floor(y),
                            (int) Math.floor(z)
                    );

                    if (isConsideredLiquidBlock(block)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // TODO - Move to BlockUtils or WorldUtils
    private static final List<Block> CONSIDERED_LIQUID_BLOCKS = List.of(
            Block.BUBBLE_COLUMN,
            Block.SUGAR_CANE,
            Block.KELP,
            Block.KELP_PLANT,
            Block.SEAGRASS,
            Block.TALL_SEAGRASS,
            Block.LILY_PAD
    );

    private static boolean isConsideredLiquidBlock(Block block) {
        if (block.liquid()) return true;
        return CONSIDERED_LIQUID_BLOCKS.contains(block);
    }
}
