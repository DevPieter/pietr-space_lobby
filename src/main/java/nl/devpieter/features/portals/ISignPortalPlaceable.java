package nl.devpieter.features.portals;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.signs.interfaces.ISignDecodable;
import nl.devpieter.features.signs.models.SignHolder;

public interface ISignPortalPlaceable extends ISignDecodable {

    Portal placePortal(Instance instance, Point point, SignHolder signHolder);
}
