package nl.devpieter.features.maze;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MazeGenerator {

    private final List<Cell> grid;
    private final Difficulty difficulty;
    private final Cell start;
    private final Cell end;

    private final Map<Position, Cell> cellsByPosition;
    private final List<Cell> cells = new ArrayList<>();

    private final Random random;

    private Cell current;

    public MazeGenerator(
            @NotNull List<Cell> grid,
            Difficulty difficulty,
            Random random,
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        this.grid = grid;
        if (this.grid.isEmpty())
            throw new IllegalArgumentException("The maze grid cannot be empty");

        this.start = findCell(startX, startY);
        if (this.start == null)
            throw new IllegalArgumentException("The maze grid does not contain a cell at the start coordinates: (" + startX + ", " + startY + ")");

        this.end = findCell(endX, endY);
        if (this.end == null)
            throw new IllegalArgumentException("The maze grid does not contain a cell at the end coordinates: (" + endX + ", " + endY + ")");

        this.difficulty = difficulty;
        this.cellsByPosition = indexCells();

        this.random = random == null ? new Random() : random;

        this.current = this.start;
        this.cells.add(this.current);
    }

    public void generateAsync(Consumer<List<Cell>> onDone) {
        new Thread(() -> {
            while (!cells.isEmpty()) carve();
            onDone.accept(grid);
        }).start();
    }

    private void carve() {
        current.setVisited(true);
        Cell next = selectNextNeighbour();

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

    private Cell selectActiveCell() {
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

    private Cell selectNextNeighbour() {
        List<Cell> neighbours = new ArrayList<>(4);

        addIfUnvisited(neighbours, current.getX(), current.getY() - 1);
        addIfUnvisited(neighbours, current.getX() + 1, current.getY());
        addIfUnvisited(neighbours, current.getX(), current.getY() + 1);
        addIfUnvisited(neighbours, current.getX() - 1, current.getY());

        if (neighbours.isEmpty()) return null;
        return neighbours.get(random.nextInt(neighbours.size()));
    }

    private void addIfUnvisited(List<Cell> neighbours, int x, int y) {
        Cell neighbour = cellsByPosition.get(new Position(x, y));
        if (neighbour != null && !neighbour.isVisited()) neighbours.add(neighbour);
    }

    private Map<Position, Cell> indexCells() {
        Map<Position, Cell> result = new HashMap<>(grid.size());

        for (Cell cell : grid) {
            Position position = new Position(cell.getX(), cell.getY());
            if (result.put(position, cell) == null) continue;

            throw new IllegalArgumentException("The maze grid contains duplicate cell coordinates: " + position);
        }

        return result;
    }

    private @Nullable Cell findCell(int x, int y) {
        for (Cell cell : grid) if (cell.getX() == x && cell.getY() == y) return cell;
        return null;
    }

    public Cell getStart() {
        return start;
    }

    public Cell getEnd() {
        return end;
    }

    private record Position(int x, int y) {
    }
}
