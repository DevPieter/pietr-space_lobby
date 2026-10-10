package nl.devpieter.features.parkour.decodables;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import nl.devpieter.features.parkour.models.parkour.ParkourNode;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.signs.interfaces.ISignDecodable;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.BooleanPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.BoundingBoxPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.IntPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.YawPitchPartDecoder;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

import java.util.HashMap;
import java.util.List;

public class ParkourNodeDecodable implements ISignDecodable {

    private int index = 0;
    private boolean isCheckpoint = false;

    private BoundingBox boundingBox = new BoundingBox(Vec.ZERO, Vec.ZERO);
    private SimpleYawPitch rotation = SimpleYawPitch.ZERO;

    public ParkourNode getNode(SignHolder signHolder) {
        return new ParkourNode(
                index,
                isCheckpoint,
                signHolder.block().instance(),
                signHolder.block().position(),
                rotation,
                boundingBox
        );
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(1, List.of(
                    new IntPartDecoder(0, i -> index = i - 1),
                    new BooleanPartDecoder(false, b -> isCheckpoint = b)
            ));
            put(2, List.of(
                    new BoundingBoxPartDecoder(new BoundingBox(1, 0.3, 1), bb -> boundingBox = bb)
            ));
            put(3, List.of(
                    new YawPitchPartDecoder(SimpleYawPitch.ZERO, r -> rotation = r)
            ));
        }};
    }
}
