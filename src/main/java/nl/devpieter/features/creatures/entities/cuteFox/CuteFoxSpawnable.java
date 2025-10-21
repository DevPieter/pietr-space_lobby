package nl.devpieter.features.creatures.entities.cuteFox;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.FoxMeta;
import net.minestom.server.instance.Instance;
import nl.devpieter.features.creatures.ISignSpawnable;
import nl.devpieter.features.creatures.entities.cuteFox.enums.CuteFoxVariant;
import nl.devpieter.features.creatures.entities.cuteFox.partDecoders.CuteFoxVariantPartDecoder;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.features.signs.partDecoders.BooleanPartDecoder;
import nl.devpieter.features.signs.partDecoders.PointPartDecoder;
import nl.devpieter.features.signs.partDecoders.YawPitchPartDecoder;
import nl.devpieter.models.YawPitch;

import java.util.HashMap;
import java.util.List;

public class CuteFoxSpawnable implements ISignSpawnable {

    private CuteFoxVariant variant = CuteFoxVariant.RED;

    private boolean isSleeping = false;
    private boolean isSitting = false;
    private boolean isBaby = false;

    private Point offset = Pos.ZERO;
    private YawPitch rotation = YawPitch.ZERO;

    @Override
    public String getIdentifier() {
        return "CuteFox";
    }

    @Override
    public Entity spawn(Instance instance, Point point, SignHolder signHolder) {
        CuteFoxEntity entity = new CuteFoxEntity();

        FoxMeta catMeta = (FoxMeta) entity.getEntityMeta();
        catMeta.setSleeping(isSleeping);
        catMeta.setSitting(isSitting);
        catMeta.setBaby(isBaby);

        entity.set(DataComponents.FOX_VARIANT, variant.getVariant());

        Pos pos = point.add(new Pos(0.5, 0, 0.5)).add(offset).asPos();
        pos = pos.withYaw(rotation.yaw()).withPitch(rotation.pitch());

        entity.setInstance(instance, pos);
        return entity;
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(1, List.of(
                    new CuteFoxVariantPartDecoder(CuteFoxVariant.RED, v -> variant = v),
                    new BooleanPartDecoder(false, b -> isSleeping = b),
                    new BooleanPartDecoder(false, b -> isSitting = b),
                    new BooleanPartDecoder(false, b -> isBaby = b)
            ));
            put(3, List.of(
                    new PointPartDecoder(Pos.ZERO, p -> offset = p)
            ));
            put(4, List.of(
                    new YawPitchPartDecoder(YawPitch.ZERO, yp -> rotation = yp)
            ));
        }};
    }
}
