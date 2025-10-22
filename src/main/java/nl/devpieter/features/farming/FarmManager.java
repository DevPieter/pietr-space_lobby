package nl.devpieter.features.farming;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.Settings;
import nl.devpieter.features.farming.blockHandlers.FarmBerryCropHandler;
import nl.devpieter.features.farming.blockHandlers.FarmCropHandler;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.utils.TextUtils;
import nl.devpieter.utils.WorldUtils;

import java.util.ArrayList;
import java.util.List;

public class FarmManager {

    private static FarmManager INSTANCE;
    private static final String SIGN_KEY = "<FARM>";

    private final ComponentLogger logger = MinecraftServer.LOGGER;
    private final SignManager signManager = SignManager.getInstance();

    private final List<BlockHolder> farmLocations = new ArrayList<>();
    private final List<Entity> debugMarkers = new ArrayList<>();

    private FarmManager() {
    }

    public static FarmManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FarmManager();
        return INSTANCE;
    }

    public void init() {
        revertFarms();
        scanFarms();
        shuffleFarms();

        if (Settings.DEBUG) placeDebugMarkers();
    }

    private void scanFarms() {
        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {
            Instance instance = sign.block().instance();

            Point point = sign.block().position().sub(0, 1, 0);
            Block block = instance.getBlock(point);

            farmLocations.add(new BlockHolder(
                    instance,
                    point,
                    block
            ));
        }
    }

    public void revertFarms() {
        clearDebugMarkers();

        if (farmLocations.isEmpty()) return;
        logger.info("Reverting {} farms to their original state.", farmLocations.size());

        for (BlockHolder block : farmLocations) {
            Instance instance = block.instance();
            instance.setBlock(block.position(), block.block());
            instance.setBlock(block.position().add(0, 1, 0), Block.AIR);
        }

        farmLocations.clear();
    }

    public void shuffleFarms() {
        for (BlockHolder block : farmLocations) {
            Instance instance = block.instance();
            Point point = block.position().add(0, 1, 0);

            Block crop = getRandomCrop();
            instance.setBlock(point, crop);

            Block blockBelow = getBlockBelow(crop);
            instance.setBlock(point.withY(point.y() - 1), blockBelow);
        }
    }

    public void placeDebugMarkers() {
        clearDebugMarkers();

        for (BlockHolder block : farmLocations) {
            debugMarkers.add(WorldUtils.placeMarker(
                    block.instance(),
                    block.position().add(0.5, 1.8, 0.5),
                    TextUtils.miniText("<#ffcf4b>Farmable")
            ));
        }
    }

    public void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }

    private Block getRandomCrop() {
        List<Block> crops = List.of(
                Block.SWEET_BERRY_BUSH.withProperty("age", "3").withHandler(new FarmBerryCropHandler()),
                Block.WHEAT.withProperty("age", "7").withHandler(new FarmCropHandler(7)),
                Block.CARROTS.withProperty("age", "7").withHandler(new FarmCropHandler(7)),
                Block.POTATOES.withProperty("age", "7").withHandler(new FarmCropHandler(7)),
                Block.BEETROOTS.withProperty("age", "3").withHandler(new FarmCropHandler(5_000, 8_000, 3))
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
}
