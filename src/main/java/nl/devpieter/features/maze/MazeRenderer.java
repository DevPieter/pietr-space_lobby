package nl.devpieter.features.maze;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.maze.models.MazeCell;
import nl.devpieter.models.SimpleVec;

import java.util.List;
import java.util.function.Function;

public final class MazeRenderer {

    private final int originX;
    private final int originY;
    private final int originZ;

    private final int corridorWidth;
    private final int wallHeight;

    private final Function<Void, Block> floorBlock;
    private final Function<Void, Block> wallBlock;

    // TODO - Use batch or idk generator for better performance, but this is fine for now
    public MazeRenderer(
            SimpleVec origin,
            int corridorWidth,
            int wallHeight,
            Function<Void, Block> floorBlock,
            Function<Void, Block> wallBlock
    ) {
        if (corridorWidth < 1) throw new IllegalArgumentException("Corridor width must be at least 1");
        if (wallHeight < 1) throw new IllegalArgumentException("Wall height must be at least 1");

        this.originX = (int) origin.x();
        this.originY = (int) origin.y();
        this.originZ = (int) origin.z();

        this.corridorWidth = corridorWidth;
        this.wallHeight = wallHeight;

        this.floorBlock = floorBlock;
        this.wallBlock = wallBlock;
    }

    public void render(Instance instance, List<MazeCell> cells, MazeCell start, MazeCell end) {
        if (cells == null || cells.isEmpty()) throw new IllegalArgumentException("The maze cannot be empty");

        int mazeWidth = cells.stream().mapToInt(MazeCell::getX).max().orElseThrow() + 1;
        int mazeDepth = cells.stream().mapToInt(MazeCell::getZ).max().orElseThrow() + 1;

        int pitch = corridorWidth + 1;
        int maxX = originX + mazeWidth * pitch;
        int maxZ = originZ + mazeDepth * pitch;

        // Floor and ceiling
        for (int x = originX; x <= maxX; x++) {
            for (int z = originZ; z <= maxZ; z++) {
                instance.setBlock(x, originY, z, floorBlock.apply(null));
                instance.setBlock(x, originY - 1, z, Block.BEDROCK);
                instance.setBlock(x, originY - 2, z, Block.BEDROCK);
                instance.setBlock(x, originY + wallHeight + 1, z, Block.BARRIER);
            }
        }

        // Walls
        for (MazeCell cell : cells) {
            int x = cellX(cell);
            int z = cellZ(cell);
            boolean[] walls = cell.getWalls();

            if (walls[0]) fillWall(instance, x, z, x + pitch, z);
            if (walls[3]) fillWall(instance, x, z, x, z + pitch);
            if (cell.getX() == mazeWidth - 1 && walls[1]) fillWall(instance, x + pitch, z, x + pitch, z + pitch);
            if (cell.getZ() == mazeDepth - 1 && walls[2]) fillWall(instance, x, z + pitch, x + pitch, z + pitch);
        }

        // Start and end
        fillFloor(instance, start, Block.LIGHT_BLUE_CONCRETE, Block.LIGHT_BLUE_STAINED_GLASS);
        fillFloor(instance, end, Block.LIME_CONCRETE, Block.LIME_STAINED_GLASS);
    }

    private int cellX(MazeCell cell) {
        return originX + cell.getX() * (corridorWidth + 1);
    }

    private int cellZ(MazeCell cell) {
        return originZ + cell.getZ() * (corridorWidth + 1);
    }

    private void fillFloor(Instance instance, MazeCell cell, Block block, Block glassBlock) {
        int x = cellX(cell);
        int z = cellZ(cell);

        for (int dx = 1; dx <= corridorWidth; dx++) {
            for (int dz = 1; dz <= corridorWidth; dz++) {
                instance.setBlock(x + dx, originY, z + dz, block);
            }
        }

        int centerX = x + (corridorWidth / 2) + 1;
        int centerZ = z + (corridorWidth / 2) + 1;
        instance.setBlock(centerX, originY, centerZ, glassBlock);
        instance.setBlock(centerX, originY - 1, centerZ, Block.BEACON);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                instance.setBlock(centerX + dx, originY - 2, centerZ + dz, Block.IRON_BLOCK);
            }
        }

    }

    private void fillWall(Instance instance, int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = originY; y < originY + wallHeight + 1; y++) {
                    instance.setBlock(x, y, z, wallBlock.apply(null));
                }
            }
        }
    }
}
