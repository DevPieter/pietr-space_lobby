package space.pietr.lobby.common.features.signs.partDecoders;

import space.pietr.lobby.common.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class FloatPartDecoder extends PartDecoder<Float> {

    private final float fallback;

    public FloatPartDecoder(float fallback, Consumer<Float> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        try {
            onDecoded(Float.parseFloat(data));
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
