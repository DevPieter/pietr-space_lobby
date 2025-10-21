package nl.devpieter.features.mobs.decoder.partDecoders;

import nl.devpieter.features.mobs.decoder.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class StringPartDecoder extends PartDecoder<String> {

    public StringPartDecoder(Consumer<String> onDecodedCallback) {
        super(onDecodedCallback);
    }

    @Override
    public void decode(String data) {
        onDecoded(data);
    }
}
