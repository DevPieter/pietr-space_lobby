package nl.devpieter.features.mobs.decoder.partDecoders;

import nl.devpieter.features.mobs.decoder.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class FloatPartDecoder extends PartDecoder<Float> {

    public FloatPartDecoder(Consumer<Float> onDecodedCallback) {
        super(onDecodedCallback);
    }

    @Override
    public void decode(String data) {
        onDecoded(Float.parseFloat(data));
    }
}
