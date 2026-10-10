package space.pietr.lobby.common.utils.minestom;

import net.kyori.adventure.text.Component;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorldUtils {

    public static Entity placeBoundingBox(@NotNull Instance instance, @NotNull Point origin, @NotNull BoundingBox box) {
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

        return entity;
    }

    public static Entity placeMarker(@NotNull Instance instance, @NotNull Point point, @NotNull Component text) {
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

//    public static @NotNull List<Point> findBlocks(Instance instance, Point center, int radius, Block targetBlock) {
//        List<Point> found = new ArrayList<>();
//
//        int minX = center.blockX() - radius;
//        int maxX = center.blockX() + radius;
//
//        int minY = Math.max(center.blockY() - radius, -64);
//        int maxY = Math.min(center.blockY() + radius, 320);
//
//        int minZ = center.blockZ() - radius;
//        int maxZ = center.blockZ() + radius;
//
//        for (int x = minX; x <= maxX; x++) {
//            for (int y = minY; y <= maxY; y++) {
//                for (int z = minZ; z <= maxZ; z++) {
//                    if (center.distance(x, y, z) > radius) continue;
//
//                    Point pos = new Vec(x, y, z);
//
//                    if (!instance.isChunkLoaded(pos)) {
//                        CompletableFuture<Chunk> future = instance.loadChunk(pos);
//                        future.join();
//                    }
//
//                    Block block = instance.getBlock(pos);
//                    if (!block.compare(targetBlock)) continue;
//
//                    found.add(pos);
//                }
//            }
//        }
//
//        return found;
//    }

    public static @NotNull List<BlockVec> findBlocks(Instance instance, Point center, int radius, Block... targetBlocks) {
        return findBlocks(instance, center, radius, List.of(targetBlocks));
    }

    public static @NotNull List<BlockVec> findBlocks(Instance instance, Point center, int radius, List<Block> targetBlocks) {
        List<BlockVec> found = new ArrayList<>();

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

                    BlockVec pos = new BlockVec(x, y, z);

                    if (!instance.isChunkLoaded(pos)) {
                        CompletableFuture<Chunk> future = instance.loadChunk(pos);
                        future.join();
                    }

                    Block block = instance.getBlock(pos);

                    for (Block targetBlock : targetBlocks) {
                        if (!block.compare(targetBlock)) continue;

                        found.add(pos);
                        break;
                    }
                }
            }
        }

        return found;
    }

    public static @NotNull List<BlockVec> findBlocks(Instance instance, BlockVec origin, BoundingBox box, Block... targetBlocks) {
        return findBlocks(instance, origin, box, List.of(targetBlocks));
    }

    public static @NotNull List<BlockVec> findBlocks(Instance instance, BlockVec origin, BoundingBox box, List<Block> targetBlocks) {
        List<BlockVec> found = new ArrayList<>();

        int minX = origin.blockX() + (int) Math.floor(box.relativeStart().x());
        int maxX = origin.blockX() + (int) Math.ceil(box.relativeEnd().x());

        int minY = Math.max(origin.blockY() + (int) Math.floor(box.relativeStart().y()), -64);
        int maxY = Math.min(origin.blockY() + (int) Math.ceil(box.relativeEnd().y()), 320);

        int minZ = origin.blockZ() + (int) Math.floor(box.relativeStart().z());
        int maxZ = origin.blockZ() + (int) Math.ceil(box.relativeEnd().z());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockVec pos = new BlockVec(x, y, z);

                    if (!instance.isChunkLoaded(pos)) {
                        CompletableFuture<Chunk> future = instance.loadChunk(pos);
                        future.join();
                    }

                    Block block = instance.getBlock(pos);

                    for (Block targetBlock : targetBlocks) {
                        if (!block.compare(targetBlock)) continue;

                        found.add(pos);
                        break;
                    }
                }
            }
        }

        return found;
    }
}
