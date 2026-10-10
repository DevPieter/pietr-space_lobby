package nl.devpieter.features.portals;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import nl.devpieter.features.portals.models.Portal;
import nl.devpieter.features.world.WorldManager;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.signs.interfaces.ISignDecodable;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.BoundingBoxPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.PointPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.StringPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.YawPitchPartDecoder;
import space.pietr.lobby.common.features.world.World;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

import java.util.HashMap;
import java.util.List;

public class PortalDecodable implements ISignDecodable {

    private final WorldManager worldManager = WorldManager.getInstance();

    private World destinationWorld;
    private Point destinationPoint = Vec.ZERO;
    private SimpleYawPitch destinationRotation = SimpleYawPitch.ZERO;

    private Point originOffset = Vec.ZERO;
    private BoundingBox boundingBox = new BoundingBox(Vec.ZERO, Vec.ZERO);

    public Portal placePortal(SignHolder signHolder) {
        Point originPos = signHolder.block().position().add(originOffset).add(0.5, 0, 0.5);

        Pos destinationPos = destinationPoint.asPos()
                .withYaw(destinationRotation.yaw())
                .withPitch(destinationRotation.pitch());

        return new Portal(
                signHolder.block().instance(),
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
                    new YawPitchPartDecoder(SimpleYawPitch.ZERO, r -> destinationRotation = r)
            ));
            put(4, List.of(
                    new PointPartDecoder(Vec.ZERO, p -> originOffset = p)
            ));
            put(5, List.of(
                    new BoundingBoxPartDecoder(bb -> boundingBox = bb)
            ));
        }};
    }
}
