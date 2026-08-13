package nl.devpieter.features.maze.models;

public class MazeCell {

    private final int x;
    private final int z;

    private boolean visited = false;
    private final boolean[] walls = {true, true, true, true};

    public boolean[] getWalls() {
        return walls;
    }

    public MazeCell(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void removeWalls(MazeCell next) {
        int x = this.x - next.x;
        // top 0, right 1, bottom 2, left 3

        if (x == 1) {
            walls[3] = false;
            next.walls[1] = false;
        } else if (x == -1) {
            walls[1] = false;
            next.walls[3] = false;
        }

        int z = this.z - next.z;

        if (z == 1) {
            walls[0] = false;
            next.walls[2] = false;
        } else if (z == -1) {
            walls[2] = false;
            next.walls[0] = false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        MazeCell other = (MazeCell) obj;
        if (x != other.x) return false;
        return z == other.z;
    }
}
