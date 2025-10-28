package nl.devpieter.config;

import nl.devpieter.utils.common.EnvUtils;

public class EnvStatics {

    public static final boolean DEBUG = EnvUtils.readBoolean("DEBUG", false);

    public static final String APP_CONFIG_PATH = EnvUtils.readString("APP_CONFIG_PATH", "/app/data/config.json");
    public static final String WORLD_DATA_PATH = EnvUtils.readString("WORLD_PATH_BASE", "/app/data/worlds");
}
