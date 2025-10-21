package nl.devpieter.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorldUtils {

    public static void placeBoundingBox(@NotNull InstanceContainer instance, @NotNull Pos origin, @NotNull BoundingBox box) {
        double sizeX = Math.abs(box.relativeEnd().x() - box.relativeStart().x());
        double sizeY = Math.abs(box.relativeEnd().y() - box.relativeStart().y());
        double sizeZ = Math.abs(box.relativeEnd().z() - box.relativeStart().z());

        double minX = Math.min(box.relativeStart().x(), box.relativeEnd().x());
        double minY = Math.min(box.relativeStart().y(), box.relativeEnd().y());
        double minZ = Math.min(box.relativeStart().z(), box.relativeEnd().z());

        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setInstance(instance, origin.add(minX, minY, minZ));

        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.GLASS);
            meta.setScale(new Vec(sizeX, sizeY, sizeZ));

            meta.setHasNoGravity(true);
        });
    }

    public static Entity placeMarker(@NotNull InstanceContainer instance, @NotNull Point point, @NotNull Component text) {
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        entity.setInstance(instance, point);

        entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
            textDisplayMeta.setText(text);
            textDisplayMeta.setScale(new Vec(0.5, 0.5, 0.5));

            textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
            textDisplayMeta.setHasNoGravity(true);
        });

        return entity;
    }

    public static @NotNull List<Point> findBlocks(Instance instance, Point center, int radius, Block targetBlock) {
        List<Point> found = new ArrayList<>();

        int minX = center.blockX() - radius;
        int maxX = center.blockX() + radius;

        int minY = Math.max(center.blockY() - radius, -64);
        int maxY = Math.min(center.blockY() + radius, 320);

        int minZ = center.blockZ() - radius;
        int maxZ = center.blockZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (center.distance(x, y, z) > radius) continue;

                    Point pos = new Vec(x, y, z);

                    if (!instance.isChunkLoaded(pos)) {
                        CompletableFuture<Chunk> future = instance.loadChunk(pos);
                        future.join();
                    }

                    Block block = instance.getBlock(pos);
                    if (!block.compare(targetBlock)) continue;

                    found.add(pos);
                }
            }
        }

        return found;
    }
}
