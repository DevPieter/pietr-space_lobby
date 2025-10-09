package nl.devpieter.managers;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import nl.devpieter.blockHandlers.FarmBerryCropHandler;
import nl.devpieter.blockHandlers.FarmCropHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FarmManager {

    private static FarmManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();
    private final List<Point> farmLocations = new ArrayList<>();

    private FarmManager() {
    }

    public static FarmManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FarmManager();
        return INSTANCE;
    }

    public void init() {
        InstanceContainer instance = worldManager.getWorld(UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"));
        if (instance == null) throw new IllegalStateException("Farm world not found");

        ComponentLogger logger = MinecraftServer.LOGGER;
        logger.info("Initializing farms...");

        if (farmLocations.isEmpty()) {
            logger.info("Searching for farm locations, this may take a while...");
            List<Point> newLocations = findBlocks(instance, new Vec(0, 64, 0), 24, Block.OXIDIZED_COPPER);
            if (newLocations.isEmpty()) {
                logger.warn("No farm locations found!");
                return;
            }

            logger.info("Found {} farm locations", newLocations.size());
            farmLocations.addAll(newLocations);
        }

        for (Point point : farmLocations) {
            point = point.add(0, 1, 0);

            Block crop = getRandomCrop();
            instance.setBlock(point, crop);

            Block blockBelow = getBlockBelow(crop);
            instance.setBlock(point.withY(point.y() - 1), blockBelow);
        }
    }

    private Block getRandomCrop() {
        List<Block> crops = List.of(
                Block.SWEET_BERRY_BUSH.withProperty("age", "3").withHandler(new FarmBerryCropHandler()),
                Block.WHEAT.withProperty("age", "7").withHandler(new FarmCropHandler(7)),
                Block.CARROTS.withProperty("age", "7").withHandler(new FarmCropHandler(7)),
                Block.POTATOES.withProperty("age", "7").withHandler(new FarmCropHandler(7)),
                Block.BEETROOTS.withProperty("age", "3").withHandler(new FarmCropHandler(3))
        );

        return crops.get((int) (Math.random() * crops.size()));
    }

    private Block getBlockBelow(Block cropBlock) {
        if (cropBlock.compare(Block.WHEAT) || cropBlock.compare(Block.CARROTS) || cropBlock.compare(Block.POTATOES) || cropBlock.compare(Block.BEETROOTS)) {
            return Block.FARMLAND.withProperty("moisture", "7");
        } else if (cropBlock.compare(Block.SWEET_BERRY_BUSH)) {
            return Block.GRASS_BLOCK;
        }

        return Block.AIR;
    }

    private @NotNull List<Point> findBlocks(Instance instance, Point center, int radius, Block targetBlock) {
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
