package nl.devpieter.features.parkour.decodables;

import nl.devpieter.features.parkour.models.parkour.ParkourLeaderboardNode;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.signs.interfaces.ISignDecodable;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;

import java.util.HashMap;
import java.util.List;

public class ParkourLeaderboardNodeDecodable implements ISignDecodable {

    public ParkourLeaderboardNode getNode(SignHolder signHolder) {
        return new ParkourLeaderboardNode(signHolder.block().instance(), signHolder.block().position());
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>();
    }
}
