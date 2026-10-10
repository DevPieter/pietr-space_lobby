package nl.devpieter.features.creatures.entities.cuteCat;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.creatures.entities.cuteCat.enums.CuteCatCollar;
import nl.devpieter.features.creatures.entities.cuteCat.enums.CuteCatVariant;
import nl.devpieter.features.creatures.entities.cuteCat.partDecoders.CuteCatCollarPartDecoder;
import nl.devpieter.features.creatures.entities.cuteCat.partDecoders.CuteCatVariantPartDecoder;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.features.signs.interfaces.ISignSpawnable;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.BooleanPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.PointPartDecoder;
import space.pietr.lobby.common.features.signs.partDecoders.YawPitchPartDecoder;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

import java.util.HashMap;
import java.util.List;

public class CuteCatSpawnable implements ISignSpawnable {

    private CuteCatVariant variant = CuteCatVariant.BLACK;
    private CuteCatCollar collar = CuteCatCollar.RED;

    private boolean isLying = false;
    private boolean isSitting = false;
    private boolean isBaby = false;

    private Point offset = Vec.ZERO;
    private SimpleYawPitch rotation = SimpleYawPitch.ZERO;

    @Override
    public String getIdentifier() {
        return "CuteCat";
    }

    @Override
    public Entity spawn(Instance instance, Point point, SignHolder signHolder) {
        CuteCatEntity entity = new CuteCatEntity();

        CatMeta catMeta = (CatMeta) entity.getEntityMeta();
        catMeta.setLying(isLying);
        catMeta.setSitting(isSitting);
        catMeta.setBaby(isBaby);

        entity.set(DataComponents.CAT_VARIANT, variant.getVariant());
        entity.set(DataComponents.CAT_COLLAR, collar.getColor());

        Pos pos = point.add(new Pos(0.5, 0, 0.5)).add(offset).asPos();
        pos = pos.withYaw(rotation.yaw()).withPitch(rotation.pitch());

        entity.setInstance(instance, pos);
        return entity;
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(1, List.of(
                    new CuteCatVariantPartDecoder(CuteCatVariant.BLACK, v -> variant = v),
                    new CuteCatCollarPartDecoder(CuteCatCollar.RED, c -> collar = c),
                    new BooleanPartDecoder(false, b -> isLying = b),
                    new BooleanPartDecoder(false, b -> isSitting = b),
                    new BooleanPartDecoder(false, b -> isBaby = b)
            ));
            put(3, List.of(
                    new PointPartDecoder(Pos.ZERO, p -> offset = p)
            ));
            put(4, List.of(
                    new YawPitchPartDecoder(SimpleYawPitch.ZERO, yp -> rotation = yp)
            ));
        }};
    }
}
