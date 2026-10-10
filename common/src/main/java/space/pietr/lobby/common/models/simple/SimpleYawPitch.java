package space.pietr.lobby.common.models.simple;

import space.pietr.lobby.common.types.IClonable;

public record SimpleYawPitch(
        float yaw,
        float pitch
) implements IClonable<SimpleYawPitch> {

    public static SimpleYawPitch ZERO = new SimpleYawPitch(0f, 0f);

    public static SimpleYawPitch of(float yaw, float pitch) {
        return new SimpleYawPitch(yaw, pitch);
    }

    @Override
    public SimpleYawPitch cloneObj() {
        return new SimpleYawPitch(yaw, pitch);
    }
}
