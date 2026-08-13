package nl.devpieter.features.maze;

public class Cell {

    private final int x;
    private final int y;

    private boolean visited = false;
    private final boolean[] walls = {true, true, true, true};

    public boolean[] getWalls() {
        return walls;
    }

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void removeWalls(Cell next) {
        int x = this.x - next.x;
        // top 0, right 1, bottom 2, left 3

        if (x == 1) {
            walls[3] = false;
            next.walls[1] = false;
        } else if (x == -1) {
            walls[1] = false;
            next.walls[3] = false;
        }

        int y = this.y - next.y;

        if (y == 1) {
            walls[0] = false;
            next.walls[2] = false;
        } else if (y == -1) {
            walls[2] = false;
            next.walls[0] = false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        Cell other = (Cell) obj;
        if (x != other.x) return false;
        return y == other.y;
    }
}
