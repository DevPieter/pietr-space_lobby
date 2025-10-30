package nl.devpieter.features.parkour;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.parkour.models.parkour.ParkourNode;
import nl.devpieter.features.signs.interfaces.ISignDecodable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.features.world.WorldManager;

import java.util.HashMap;
import java.util.List;

public class ParkourDecodable implements ISignDecodable {

    private final WorldManager worldManager = WorldManager.getInstance();

    public ParkourNode placeNode(Instance instance, Point point, SignHolder signHolder) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {};
    }
}
