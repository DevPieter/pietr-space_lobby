package nl.devpieter.features.parkour.decodables;

import nl.devpieter.features.parkour.models.parkour.ParkourSpawnNode;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.signs.interfaces.ISignDecodable;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.YawPitchPartDecoder;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

import java.util.HashMap;
import java.util.List;

public class ParkourSpawnNodeDecodable implements ISignDecodable {

    private SimpleYawPitch rotation = SimpleYawPitch.ZERO;

    public ParkourSpawnNode getNode(SignHolder signHolder) {
        return new ParkourSpawnNode(signHolder.block().instance(), signHolder.block().position(), rotation);
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(2, List.of(
                    new YawPitchPartDecoder(SimpleYawPitch.ZERO, r -> rotation = r)
            ));
        }};
    }
}
