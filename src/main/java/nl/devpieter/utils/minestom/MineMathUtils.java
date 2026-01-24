package nl.devpieter.utils.minestom;

import net.minestom.server.utils.Direction;
import org.jspecify.annotations.NonNull;

public class MineMathUtils {

    public static Direction getFacingDirection(@NonNull String facingProperty) {
        return switch (facingProperty) {
            case "south" -> Direction.SOUTH;
            case "west" -> Direction.WEST;
            case "east" -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public static float getYawFromDirection(@NonNull Direction direction) {
        return switch (direction) {
            case SOUTH -> 180.0f;
            case WEST -> 90.0f;
            case EAST -> -90.0f;
            default -> 0.0f;
        };
    }
}
