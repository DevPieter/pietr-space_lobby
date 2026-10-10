package space.pietr.lobby.api.features.signs;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import space.pietr.lobby.common.features.signs.SignHolder;

import java.util.List;

public interface ISignManager {

    void scanSigns(Instance instance, BlockVec origin, BoundingBox box);

    List<SignHolder> getMatchedSigns(String key);
}
