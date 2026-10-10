package space.pietr.lobby.common.features.signs.partDecoders;

import space.pietr.lobby.common.features.signs.partDecoder.PartDecoder;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;


import java.util.function.Consumer;

public class YawPitchPartDecoder extends PartDecoder<SimpleYawPitch> {

    private final SimpleYawPitch fallback;

    public YawPitchPartDecoder(SimpleYawPitch fallback, Consumer<SimpleYawPitch> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        String[] parts = data.split(",");
        if (parts.length != 2) {
            onDecoded(fallback);
            return;
        }

        try {
            float yaw = Float.parseFloat(parts[0]);
            float pitch = Float.parseFloat(parts[1]);

            onDecoded(new SimpleYawPitch(yaw, pitch));
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
