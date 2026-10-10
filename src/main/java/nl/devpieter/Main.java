package nl.devpieter;

import net.minestom.server.MinecraftServer;
import space.pietr.lobby.common.EnvStatics;

public class Main {

    static void main(String[] args) {
        if (EnvStatics.DEBUG) MinecraftServer.LOGGER.warn("Debug mode is enabled");

        Lobby.getInstance();
    }
}
