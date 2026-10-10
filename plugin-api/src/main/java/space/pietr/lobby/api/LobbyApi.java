package space.pietr.lobby.api;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import space.pietr.lobby.api.features.signs.ISignManager;
import space.pietr.lobby.api.features.world.IWorldManager;

public interface LobbyApi {

    ISignManager sign();

    IWorldManager world();

    static ComponentLogger getLogger(Class<?> clazz) {
        return ComponentLogger.logger(clazz);
    }
}
