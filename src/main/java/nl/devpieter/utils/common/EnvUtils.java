package nl.devpieter.utils.common;

public class EnvUtils {

    public static String readString(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }

    public static boolean readBoolean(String key, boolean fallback) {
        String value = System.getenv(key);
        if (value == null) return fallback;

        return switch (value.toLowerCase()) {
            case "true", "1", "yes" -> true;
            case "false", "0", "no" -> false;
            default -> fallback;
        };
    }

    public static int readPort(String key, int fallback) {
        String value = System.getenv(key);
        if (value == null) return fallback;

        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 65535) return fallback;

            return port;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

//    public static int readInt(String key, int fallback) {
//        String value = System.getenv(key);
//        return value == null ? fallback : Integer.parseInt(value);
//    }
//
//    public static float readFloat(String key, float fallback) {
//        String value = System.getenv(key);
//        return value == null ? fallback : Float.parseFloat(value);
//    }

//    public static Pos readPos(String key, Pos fallback) {
//        String value = System.getenv(key);
//        if (value == null) return fallback;
//
//        String[] parts = value.split(",");
//        if (parts.length < 3 || parts.length > 5) return fallback;
//
//        try {
//            float x = Float.parseFloat(parts[0]);
//            float y = Float.parseFloat(parts[1]);
//            float z = Float.parseFloat(parts[2]);
//
//            float yaw = parts.length > 3 ? Float.parseFloat(parts[3]) : 0;
//            float pitch = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
//
//            return new Pos(x, y, z, yaw, pitch);
//        } catch (NumberFormatException e) {
//            return fallback;
//        }
//    }
}
