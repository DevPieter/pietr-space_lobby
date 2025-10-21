package nl.devpieter.features.mobs.decoder.partDecoders;

import nl.devpieter.features.mobs.decoder.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class IntPartDecoder extends PartDecoder<Integer> {

    public IntPartDecoder(Consumer<Integer> onDecodedCallback) {
        super(onDecodedCallback);
    }

    @Override
    public void decode(String data) {
        onDecoded(Integer.parseInt(data));
    }
}
