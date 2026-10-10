package space.pietr.lobby.common.utils.common;

/**
 * Utility class for generating random values from <a href="https://github.com/DevPieter/Utilize/blob/main/src/main/java/nl/devpieter/utilize/utils/common/RandomUtils.java">Utilize</a>.
 */
public final class RandomUtils {

    private RandomUtils() {
    }

    /**
     * @param chanceForTrue The chance to return true (0 to 100).
     * @return True with the specified probability percentage.
     */
    public static boolean randomChance(double chanceForTrue) {
        if (chanceForTrue <= 0) return false;
        if (chanceForTrue >= 100) return true;

        return Math.random() * 100 < chanceForTrue;
    }

    /**
     * @param min The minimum value (inclusive).
     * @param max The maximum value (inclusive).
     * @return A random integer between min and max, inclusive.
     */
    public static int randomIntInclusive(int min, int max) {
        if (min == max) return min;

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static int randomIntExclusive(int min, int max) {
        if (min == max) return min;

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        return (int) (Math.random() * (max - min)) + min;
    }

    /**
     * @param min       The minimum value (inclusive).
     * @param max       The maximum value (inclusive).
     * @param precision The precision to round the result to. (e.g., 0.01 for two decimal places).
     * @return A random double between min and max, inclusive, rounded to the specified precision.
     */
    public static double randomDoubleInclusive(double min, double max, double precision) {
        if (precision <= 0) throw new IllegalArgumentException("Precision must be positive.");
        if (min == max) return min;

        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        double range = max - min;
        double randomValue = Math.random() * range + min;

        double rounded = Math.round(randomValue / precision) * precision;
        return Math.min(rounded, max);
    }

    /**
     * @param min       The minimum value (inclusive).
     * @param max       The maximum value (inclusive).
     * @param precision The precision to round the result to. (e.g., 0.01f for two decimal places).
     * @return A random float between min and max, inclusive, rounded to the specified precision.
     */
    public static float randomFloatInclusive(float min, float max, float precision) {
        if (precision <= 0f) throw new IllegalArgumentException("Precision must be positive.");
        if (min == max) return min;

        if (min > max) {
            float temp = min;
            min = max;
            max = temp;
        }

        float range = max - min;
        float randomValue = (float) Math.random() * range + min;

        float rounded = Math.round(randomValue / precision) * precision;
        return Math.min(rounded, max);
    }

    public static long randomLongInclusive(long min, long max) {
        if (min == max) return min;

        if (min > max) {
            long temp = min;
            min = max;
            max = temp;
        }

        return (long) (Math.random() * (max - min + 1)) + min;
    }
}
