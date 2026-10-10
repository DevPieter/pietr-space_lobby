package space.pietr.lobby.common.features.signs.partDecoders;

import space.pietr.lobby.common.features.signs.partDecoder.PartDecoder;

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
