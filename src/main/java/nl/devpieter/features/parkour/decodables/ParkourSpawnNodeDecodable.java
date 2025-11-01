package nl.devpieter.features.parkour.decodables;

import nl.devpieter.features.parkour.models.parkour.ParkourSpawnNode;
import nl.devpieter.features.signs.interfaces.ISignDecodable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.features.signs.partDecoders.YawPitchPartDecoder;
import nl.devpieter.models.YawPitch;

import java.util.HashMap;
import java.util.List;

public class ParkourSpawnNodeDecodable implements ISignDecodable {

    private YawPitch rotation = YawPitch.ZERO;

    public ParkourSpawnNode getNode(SignHolder signHolder) {
        return new ParkourSpawnNode(signHolder.block().position(), rotation);
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(2, List.of(
                    new YawPitchPartDecoder(YawPitch.ZERO, r -> rotation = r)
            ));
        }};
    }
}
