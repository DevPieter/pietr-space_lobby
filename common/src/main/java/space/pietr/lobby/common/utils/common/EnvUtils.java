package space.pietr.lobby.common.utils.common;

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
}
