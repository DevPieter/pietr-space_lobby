package nl.devpieter.features.mobs.decoder.partDecoder;

public interface IPartDecoder<T> {

    void decode(String data);

    void onDecoded(T value);
}
