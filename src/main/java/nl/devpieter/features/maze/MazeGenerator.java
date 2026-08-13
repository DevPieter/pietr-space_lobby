package nl.devpieter.features.maze;

import nl.devpieter.features.maze.enums.MazeDifficulty;
import nl.devpieter.features.maze.models.MazeBlueprint;
import nl.devpieter.features.maze.models.MazeCell;
import nl.devpieter.models.Simple2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MazeGenerator {

    private final List<MazeCell> grid = new ArrayList<>();
    private final List<MazeCell> cells = new ArrayList<>();
    private final Map<Simple2D, MazeCell> cellsByPosition;

    private final MazeDifficulty difficulty;
    private final MazeCell start;
    private final MazeCell end;

    private final Random random;

    private MazeCell current;

    public MazeGenerator(
            @NotNull MazeBlueprint blueprint,
            @NotNull Random random
    ) {
        for (int i = 0; i < blueprint.size(); i++)
            for (int j = 0; j < blueprint.size(); j++) this.grid.add(new MazeCell(i, j));

        if (this.grid.isEmpty())
            throw new IllegalArgumentException("The maze grid cannot be empty");

        this.cellsByPosition = indexCells();

        this.start = findCell(blueprint.localStart());
        if (this.start == null)
            throw new IllegalArgumentException("The maze grid does not contain a cell at the localStart coordinates: " + blueprint.localStart());

        this.end = findCell(blueprint.localEnd());
        if (this.end == null)
            throw new IllegalArgumentException("The maze grid does not contain a cell at the localEnd coordinates: " + blueprint.localEnd());

        this.difficulty = blueprint.difficulty();
        this.random = random;

        this.current = this.start;
        this.cells.add(this.current);
    }

    public void generateAsync(Consumer<List<MazeCell>> onDone) {
        new Thread(() -> {
            while (!cells.isEmpty()) carve();
            onDone.accept(grid);
        }).start();
    }

    private void carve() {
        current.setVisited(true);
        MazeCell next = selectNextNeighbour();

        if (next != null) {
            current.removeWalls(next);
            current = next;
            cells.add(current);
        } else {
            cells.remove(current);
            if (cells.isEmpty()) return;
            current = selectActiveCell();
        }
    }

    private MazeCell selectActiveCell() {
        int newestChance = switch (difficulty) {
            case BABBY -> 5;
            case EASY -> 25;
            case MEDIUM -> 55;
            case HARD -> 85;
            case NIGHTMARE -> 95;
        };

        return random.nextInt(100) < newestChance ? cells.getLast()
                : cells.get(random.nextInt(cells.size()));
    }

    private MazeCell selectNextNeighbour() {
        List<MazeCell> neighbours = new ArrayList<>(4);

        addIfUnvisited(neighbours, current.getX(), current.getZ() - 1);
        addIfUnvisited(neighbours, current.getX() + 1, current.getZ());
        addIfUnvisited(neighbours, current.getX(), current.getZ() + 1);
        addIfUnvisited(neighbours, current.getX() - 1, current.getZ());

        if (neighbours.isEmpty()) return null;
        return neighbours.get(random.nextInt(neighbours.size()));
    }

    private void addIfUnvisited(List<MazeCell> neighbours, int x, int y) {
        MazeCell neighbour = cellsByPosition.get(new Simple2D(x, y));
        if (neighbour != null && !neighbour.isVisited()) neighbours.add(neighbour);
    }

    private Map<Simple2D, MazeCell> indexCells() {
        Map<Simple2D, MazeCell> result = new HashMap<>(grid.size());

        for (MazeCell cell : grid) {
            Simple2D position = new Simple2D(cell.getX(), cell.getZ());
            if (result.put(position, cell) == null) continue;

            throw new IllegalArgumentException("The maze grid contains duplicate cell coordinates: " + position);
        }

        return result;
    }

    private @Nullable MazeCell findCell(Simple2D position) {
        return cellsByPosition.get(position);
    }

    public MazeCell getStart() {
        return start;
    }

    public MazeCell getEnd() {
        return end;
    }
}
