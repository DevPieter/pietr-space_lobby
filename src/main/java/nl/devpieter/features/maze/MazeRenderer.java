package nl.devpieter.features.maze;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.List;
import java.util.function.Function;

public final class MazeRenderer {

    private final int originX;
    private final int originZ;
    private final int floorY;
    private final int wallY;
    private final int corridorWidth;
    private final int wallHeight;

    private final Function<Void, Block> floorBlock;
    private final Function<Void, Block> wallBlock;

    // TODO - Use batch or idk generator for better performance, but this is fine for now
    public MazeRenderer(
            int originX,
            int originZ,
            int floorY,
            int wallY,
            int corridorWidth,
            int wallHeight,
            Function<Void, Block> floorBlock,
            Function<Void, Block> wallBlock
    ) {
        if (corridorWidth < 1) throw new IllegalArgumentException("Corridor width must be at least 1");
        if (wallHeight < 1) throw new IllegalArgumentException("Wall height must be at least 1");

        this.originX = originX;
        this.originZ = originZ;
        this.floorY = floorY;
        this.wallY = wallY;
        this.corridorWidth = corridorWidth;
        this.wallHeight = wallHeight;
        this.floorBlock = floorBlock;
        this.wallBlock = wallBlock;
    }

    public void render(Instance instance, List<Cell> cells, Cell start, Cell end) {
        if (cells == null || cells.isEmpty()) throw new IllegalArgumentException("The maze cannot be empty");

        int mazeWidth = cells.stream().mapToInt(Cell::getX).max().orElseThrow() + 1;
        int mazeDepth = cells.stream().mapToInt(Cell::getY).max().orElseThrow() + 1;
        int pitch = corridorWidth + 1;
        int maxX = originX + mazeWidth * pitch;
        int maxZ = originZ + mazeDepth * pitch;

        for (int x = originX; x <= maxX; x++) {
            for (int z = originZ; z <= maxZ; z++) {
                instance.setBlock(x, floorY, z, floorBlock.apply(null));
                instance.setBlock(x, floorY - 1, z, Block.BEDROCK);
                instance.setBlock(x, floorY - 2, z, Block.BEDROCK);
                instance.setBlock(x, floorY + wallHeight + 1, z, Block.BARRIER);

                // TODO - Not needed when only generating one maze at a time
                clearWallColumn(instance, x, z);
            }
        }

        for (Cell cell : cells) {
            int x = cellX(cell);
            int z = cellZ(cell);
            boolean[] walls = cell.getWalls();

            if (walls[0]) fillWall(instance, x, z, x + pitch, z);
            if (walls[3]) fillWall(instance, x, z, x, z + pitch);
            if (cell.getX() == mazeWidth - 1 && walls[1]) fillWall(instance, x + pitch, z, x + pitch, z + pitch);
            if (cell.getY() == mazeDepth - 1 && walls[2]) fillWall(instance, x, z + pitch, x + pitch, z + pitch);
        }

        // Points on the outside edge become real entrances/exits in the outer wall.
//        openBoundary(instance, start, mazeWidth, mazeDepth, true);
//        openBoundary(instance, end, mazeWidth, mazeDepth, false);

        fillFloor(instance, start, Block.LIGHT_BLUE_CONCRETE, Block.LIGHT_BLUE_STAINED_GLASS);
        fillFloor(instance, end, Block.LIME_CONCRETE, Block.LIME_STAINED_GLASS);
    }

    private int cellX(Cell cell) {
        return originX + cell.getX() * (corridorWidth + 1);
    }

    private int cellZ(Cell cell) {
        return originZ + cell.getY() * (corridorWidth + 1);
    }

    private void clearWallColumn(Instance instance, int x, int z) {
        for (int y = wallY; y < wallY + wallHeight; y++) instance.setBlock(x, y, z, Block.AIR);
    }

    private void fillFloor(Instance instance, Cell cell, Block block, Block glassBlock) {
        int x = cellX(cell);
        int z = cellZ(cell);

        for (int dx = 1; dx <= corridorWidth; dx++) {
            for (int dz = 1; dz <= corridorWidth; dz++) {
                instance.setBlock(x + dx, floorY, z + dz, block);
            }
        }

        int centerX = x + (corridorWidth / 2) + 1;
        int centerZ = z + (corridorWidth / 2) + 1;
        instance.setBlock(centerX, floorY, centerZ, glassBlock);
        instance.setBlock(centerX, floorY - 1, centerZ, Block.BEACON);

        // Place iron blocks below the beacon to complete the beacon base
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                instance.setBlock(centerX + dx, floorY - 2, centerZ + dz, Block.IRON_BLOCK);
            }
        }

    }

    private void fillWall(Instance instance, int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = wallY - 1; y < wallY + wallHeight; y++) {
                    instance.setBlock(x, y, z, wallBlock.apply(null));
                }
            }
        }
    }

    private void openBoundary(Instance instance, Cell cell, int mazeWidth, int mazeDepth, boolean entrance) {
        if (cell == null) return;

        int x = cellX(cell);
        int z = cellZ(cell);
        int pitch = corridorWidth + 1;

        // Prefer opposite sides for the usual top-left / bottom-right configuration.
        if (entrance && cell.getY() == 0) {
            clearOpening(instance, x + 1, z, x + corridorWidth, z);
        } else if (!entrance && cell.getY() == mazeDepth - 1) {
            clearOpening(instance, x + 1, z + pitch, x + corridorWidth, z + pitch);
        } else if (cell.getX() == 0) {
            clearOpening(instance, x, z + 1, x, z + corridorWidth);
        } else if (cell.getX() == mazeWidth - 1) {
            clearOpening(instance, x + pitch, z + 1, x + pitch, z + corridorWidth);
        }
    }

    private void clearOpening(Instance instance, int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) clearWallColumn(instance, x, z);
        }
    }
}
