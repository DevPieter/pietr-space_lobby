package nl.devpieter.features.signs.partDecoders;

import nl.devpieter.features.signs.partDecoder.PartDecoder;
import nl.devpieter.models.YawPitch;

import java.util.function.Consumer;

public class YawPitchPartDecoder extends PartDecoder<YawPitch> {

    private final YawPitch fallback;

    public YawPitchPartDecoder(YawPitch fallback, Consumer<YawPitch> onDecodedCallback) {
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

            onDecoded(new YawPitch(yaw, pitch));
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
