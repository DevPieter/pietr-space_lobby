package nl.devpieter.features.mobs.decoder.partDecoder;

import java.util.function.Consumer;

public abstract class PartDecoder<T> implements IPartDecoder<T> {

    private final Consumer<T> onDecodedCallback;

    public PartDecoder(Consumer<T> onDecodedCallback) {
        this.onDecodedCallback = onDecodedCallback;
    }

    @Override
    public void onDecoded(T value) {
        onDecodedCallback.accept(value);
    }
}
