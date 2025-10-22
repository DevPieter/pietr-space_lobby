package nl.devpieter;

import nl.devpieter.utils.EnvUtils;

public class Settings {

    public static final boolean DEBUG = EnvUtils.readBoolean("DEBUG", false);

    public static final String API_BASE_URL = EnvUtils.readString("API_BASE_URL", "http://localhost:5193");

    public static final boolean USE_VELOCITY_PROXY = EnvUtils.readBoolean("USE_VELOCITY_PROXY", false);
    public static final String VELOCITY_PROXY_SECRET = EnvUtils.readString("VELOCITY_PROXY_SECRET", "abcdefghijklmnop");

    public static final String WORLD_PATH_BASE = EnvUtils.readString("WORLD_PATH_BASE", "/app/world");
}
