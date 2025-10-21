package nl.devpieter.features.creatures;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.signs.ISignDecodable;
import nl.devpieter.features.signs.models.SignHolder;

public interface ISignSpawnable extends ISignDecodable {

    String getIdentifier();

    Entity spawn(Instance instance, Point point, SignHolder signHolder);
}
