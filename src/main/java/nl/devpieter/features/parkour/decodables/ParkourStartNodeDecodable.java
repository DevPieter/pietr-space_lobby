package nl.devpieter.features.parkour.decodables;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import nl.devpieter.features.parkour.models.parkour.ParkourStartNode;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.signs.interfaces.ISignDecodable;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.BoundingBoxPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.YawPitchPartDecoder;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

import java.util.HashMap;
import java.util.List;

public class ParkourStartNodeDecodable implements ISignDecodable {

    private BoundingBox boundingBox = new BoundingBox(Vec.ZERO, Vec.ZERO);
    private SimpleYawPitch rotation = SimpleYawPitch.ZERO;

    public ParkourStartNode getNode(SignHolder signHolder) {
        return new ParkourStartNode(
                signHolder.block().instance(),
                signHolder.block().position(),
                boundingBox,
                rotation
        );
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(2, List.of(
                    new BoundingBoxPartDecoder(new BoundingBox(1, 0.3, 1), bb -> boundingBox = bb)
            ));
            put(3, List.of(
                    new YawPitchPartDecoder(SimpleYawPitch.ZERO, r -> rotation = r)
            ));
        }};
    }
}
