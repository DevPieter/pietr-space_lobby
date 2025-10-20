package nl.devpieter;

import net.minestom.server.coordinate.Pos;
import nl.devpieter.utils.EnvUtils;

public class Settings {

    public static final boolean DEBUG = EnvUtils.readBoolean("DEBUG", false);

    public static final String API_BASE_URL = EnvUtils.readString("API_BASE_URL", "http://localhost:5193");

    public static final boolean USE_VELOCITY_PROXY = EnvUtils.readBoolean("USE_VELOCITY_PROXY", false);
    public static final String VELOCITY_PROXY_SECRET = EnvUtils.readString("VELOCITY_PROXY_SECRET", "abcdefghijklmnop");

    public static final String WORLD_PATH_BASE = EnvUtils.readString("WORLD_PATH_BASE", "/app/world");

    public static final Pos MAIN_WORLD_SPAWN = EnvUtils.readPos("WORLD_SPAWN", new Pos(0.5, 64, 0.5, -90, 0));
//    public static final int MAIN_WORLD_TIME = EnvUtils.readInt("WORLD_TIME", 13000);
    public static final int MAIN_WORLD_TIME = EnvUtils.readInt("WORLD_TIME", 0);
    public static final int MAIN_WORLD_TIME_RATE = EnvUtils.readInt("WORLD_TIME_RATE", 0);

    public static final Pos SPECIAL_WORLD_SPAWN = EnvUtils.readPos("SPECIAL_WORLD_SPAWN", new Pos(0.5, 64, 0.5, -90, 0));
    public static final int SPECIAL_WORLD_TIME = EnvUtils.readInt("SPECIAL_WORLD_TIME", 13000);
    public static final int SPECIAL_WORLD_TIME_RATE = EnvUtils.readInt("SPECIAL_WORLD_TIME_RATE", 0);
}
