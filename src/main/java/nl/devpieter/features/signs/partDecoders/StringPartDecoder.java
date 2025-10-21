package nl.devpieter.features.signs.partDecoders;

import nl.devpieter.features.signs.partDecoder.PartDecoder;

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
