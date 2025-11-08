package nl.devpieter.models;

import net.minestom.server.coordinate.Point;

public record HashPoint(int x, int y, int z) {

    public HashPoint(Point point) {
        this(point.blockX(), point.blockY(), point.blockZ());
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        result = 31 * result + Integer.hashCode(z);

        return result;
    }
}
