package nl.devpieter.features.portals;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.features.signs.partDecoders.BoundingBoxPartDecoder;
import nl.devpieter.features.signs.partDecoders.PointPartDecoder;
import nl.devpieter.features.signs.partDecoders.StringPartDecoder;
import nl.devpieter.features.signs.partDecoders.YawPitchPartDecoder;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.World;
import nl.devpieter.models.YawPitch;

import java.util.HashMap;
import java.util.List;

public class PortalDecodable implements ISignPortalPlaceable {

    private final WorldManager worldManager = WorldManager.getInstance();

    private World destinationWorld;
    private Point destinationPoint = Vec.ZERO;
    private YawPitch destinationRotation = YawPitch.ZERO;

    private Point originOffset = Vec.ZERO;
    private BoundingBox boundingBox = new BoundingBox(1, 2, 1);

    @Override
    public Portal placePortal(Instance instance, Point point, SignHolder signHolder) {
        Point originPos = point.add(originOffset).add(0.5, 0, 0.5);

        Pos destinationPos = destinationPoint.asPos()
                .withYaw(destinationRotation.yaw())
                .withPitch(destinationRotation.pitch());

        return new Portal(
                instance,
                destinationWorld,
                destinationPos,
                originPos,
                boundingBox
        );
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(1, List.of(
                    new StringPartDecoder(s -> destinationWorld = worldManager.getWorldById(s))
            ));
            put(2, List.of(
                    new PointPartDecoder(Vec.ZERO, p -> destinationPoint = p)
            ));
            put(3, List.of(
                    new YawPitchPartDecoder(YawPitch.ZERO, r -> destinationRotation = r)
            ));
            put(4, List.of(
                    new PointPartDecoder(Vec.ZERO, p -> originOffset = p)
            ));
            put(5, List.of(
                    new BoundingBoxPartDecoder(new BoundingBox(1, 2, 1), bb -> boundingBox = bb)
            ));
        }};
    }
}
