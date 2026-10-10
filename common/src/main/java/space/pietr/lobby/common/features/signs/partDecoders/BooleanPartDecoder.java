package space.pietr.lobby.common.features.signs.partDecoders;

import space.pietr.lobby.common.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class BooleanPartDecoder extends PartDecoder<Boolean> {

    private final boolean fallback;

    public BooleanPartDecoder(boolean fallback, Consumer<Boolean> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        if (data.equals("1") || data.equalsIgnoreCase("t") || data.equalsIgnoreCase("true")) {
            onDecoded(true);
        } else if (data.equals("0") || data.equalsIgnoreCase("f") || data.equalsIgnoreCase("false")) {
            onDecoded(false);
        } else {
            onDecoded(fallback);
        }
    }
}
