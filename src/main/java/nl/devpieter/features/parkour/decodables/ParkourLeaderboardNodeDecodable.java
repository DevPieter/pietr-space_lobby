package nl.devpieter.features.parkour.decodables;

import nl.devpieter.features.parkour.models.parkour.ParkourLeaderboardNode;
import nl.devpieter.features.signs.interfaces.ISignDecodable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;

import java.util.HashMap;
import java.util.List;

public class ParkourLeaderboardNodeDecodable implements ISignDecodable {

    public ParkourLeaderboardNode getNode(SignHolder signHolder) {
        return new ParkourLeaderboardNode(signHolder.block().position());
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>();
    }
}
