package space.pietr.lobby.common.features.signs.interfaces;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import space.pietr.lobby.common.features.signs.SignHolder;

public interface ISignSpawnable extends ISignDecodable {

    String getIdentifier();

    Entity spawn(Instance instance, Point point, SignHolder signHolder);
}
