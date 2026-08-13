package nl.devpieter.features.maze.models;

import nl.devpieter.features.maze.enums.MazeDifficulty;
import nl.devpieter.models.Simple2D;
import nl.devpieter.models.SimplePoint;
import org.jetbrains.annotations.NotNull;

public record MazeBlueprint(
        int seed,
        int size,
        int corridorWidth,
        int wallHeight,
        MazeDifficulty difficulty,
        Simple2D localStart,
        Simple2D localEnd
) {

    public @NotNull SimplePoint getWorldSpawn(SimplePoint worldOrigin) {
        SimplePoint worldPoint = toWorldPoint(localStart, worldOrigin);
        double spawnOffset = corridorWidth / 2.0 + 1.0;

        return new SimplePoint(
                worldPoint.x() + spawnOffset,
                worldPoint.y() + 1,
                worldPoint.z() + spawnOffset,
                0, 0
        );
    }

    public @NotNull SimplePoint getWorldEnd(SimplePoint worldOrigin) {
        SimplePoint worldPoint = toWorldPoint(localEnd, worldOrigin);
        double spawnOffset = corridorWidth / 2.0 + 1.0;

        return new SimplePoint(
                worldPoint.x() + spawnOffset,
                worldPoint.y() + 1,
                worldPoint.z() + spawnOffset,
                0, 0
        );
    }

    private @NotNull SimplePoint toWorldPoint(Simple2D localPoint, SimplePoint worldOrigin) {
        double cellStride = corridorWidth + 1.0;

        return new SimplePoint(
                worldOrigin.x() + (localPoint.x() * cellStride),
                worldOrigin.y(),
                worldOrigin.z() + (localPoint.z() * cellStride),
                0, 0
        );
    }
}
