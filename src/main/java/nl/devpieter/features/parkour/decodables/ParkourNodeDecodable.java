package nl.devpieter.features.parkour.decodables;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import nl.devpieter.features.parkour.models.parkour.ParkourNode;
import nl.devpieter.features.signs.interfaces.ISignDecodable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.features.signs.partDecoders.BooleanPartDecoder;
import nl.devpieter.features.signs.partDecoders.BoundingBoxPartDecoder;
import nl.devpieter.features.signs.partDecoders.IntPartDecoder;
import nl.devpieter.features.signs.partDecoders.YawPitchPartDecoder;
import nl.devpieter.models.YawPitch;

import java.util.HashMap;
import java.util.List;

public class ParkourNodeDecodable implements ISignDecodable {

    private int index = 0;
    private boolean isCheckpoint = false;

    private BoundingBox boundingBox = new BoundingBox(Vec.ZERO, Vec.ZERO);
    private YawPitch rotation = YawPitch.ZERO;

    public ParkourNode getNode(SignHolder signHolder) {
        return new ParkourNode(
                index,
                isCheckpoint,
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
                    new YawPitchPartDecoder(YawPitch.ZERO, r -> rotation = r)
            ));
        }};
    }
}
