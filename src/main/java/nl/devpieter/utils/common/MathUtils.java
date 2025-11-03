package nl.devpieter.utils.common;

import java.util.Random;

public class MathUtils {

    private static final Random RANDOM = new Random();

    public static double nextTriangular(double mode, double deviation) {
        return mode + deviation * (RANDOM.nextDouble() - RANDOM.nextDouble());
    }

    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(min, max + 1);
    }

    public static float randomFloat(float min, float max) {
        return min + (max - min) * RANDOM.nextFloat();
    }
}
