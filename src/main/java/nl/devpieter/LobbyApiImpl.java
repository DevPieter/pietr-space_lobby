package nl.devpieter;

import space.pietr.lobby.api.LobbyApi;
import space.pietr.lobby.api.features.signs.ISignManager;
import space.pietr.lobby.api.features.world.IWorldManager;

public record LobbyApiImpl(
        ISignManager sign,
        IWorldManager world
) implements LobbyApi {
}
