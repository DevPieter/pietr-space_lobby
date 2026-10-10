package space.pietr.lobby.common.features.signs.partDecoders;

import space.pietr.lobby.common.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class IntPartDecoder extends PartDecoder<Integer> {

    private final int fallback;

    public IntPartDecoder(int fallback, Consumer<Integer> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        try {
            onDecoded(Integer.parseInt(data));
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
