package space.pietr.lobby.common.utils.common;

import java.util.Random;

public class MathUtils {

    private static final Random RANDOM = new Random();

    public static double nextTriangular(double mode, double deviation) {
        return mode + deviation * (RANDOM.nextDouble() - RANDOM.nextDouble());
    }
}
