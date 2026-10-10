package space.pietr.lobby.api.features.world;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;
import space.pietr.lobby.common.features.world.LoadedWorld;
import space.pietr.lobby.common.features.world.World;
import space.pietr.lobby.common.features.world.enums.LoadedWorldType;
import space.pietr.lobby.common.features.world.settings.WorldSettings;
import space.pietr.lobby.common.models.tuple.Pair;

import java.util.List;

public interface IWorldManager {

    boolean createWorldFolder(String id, String name, WorldSettings settings);

    @Nullable LoadedWorld loadDynamicWorld(String id, String name, WorldSettings settings);

    boolean reloadLoadedWorldById(String id);

    List<World> getFoundWorldsFromDisk();

    List<LoadedWorld> getLoadedWorlds();

    List<String> getLoadedWorldIds();

    @Nullable LoadedWorld getMainWorld();

    @Nullable World getWorldById(@Nullable String id);

    @Nullable Pair<World, LoadedWorldType> getWorldAndTypeById(@Nullable String id);

    @Nullable LoadedWorld getLoadedWorldById(@Nullable String id);

    @Nullable LoadedWorld getLoadedWorldByInstance(@Nullable Instance instance);
}
