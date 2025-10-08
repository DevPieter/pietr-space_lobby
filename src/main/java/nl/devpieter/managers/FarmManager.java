package nl.devpieter.managers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import nl.devpieter.blockHandlers.FarmBerryCropHandler;
import nl.devpieter.blockHandlers.FarmWheatCropHandler;

import java.util.List;
import java.util.UUID;

public class FarmManager {

    private static FarmManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();

    private final List<Pos> farmLocations = List.of(
            new Pos(-6, 64, -1),
            new Pos(-5, 64, -2),
            new Pos(-4, 64, -3),
            new Pos(-5, 64, -3),
            new Pos(-5, 64, -4),
            new Pos(-4, 64, -4),
            new Pos(-7, 64, -3),
            new Pos(-8, 64, -3),
            new Pos(-8, 64, -2)
    );

    private FarmManager() {
    }

    public static FarmManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FarmManager();
        return INSTANCE;
    }

    public void init() {
        InstanceContainer instance = worldManager.getWorld(UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"));
        if (instance == null) throw new IllegalStateException("Farm world not found");

        for (Pos pos : farmLocations) {
            Block crop = getRandomCrop();
            instance.setBlock(pos, crop);
        }
    }

    private Block getRandomCrop() {
        List<Block> crops = List.of(
                Block.SWEET_BERRY_BUSH.withProperty("age", "3").withHandler(new FarmBerryCropHandler()),
                Block.WHEAT.withProperty("age", "7").withHandler(new FarmWheatCropHandler())
        );

        return crops.get((int) (Math.random() * crops.size()));
    }
}
