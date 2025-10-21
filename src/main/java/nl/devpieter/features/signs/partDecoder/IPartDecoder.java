package nl.devpieter.features.signs.partDecoder;

public interface IPartDecoder<T> {

    void decode(String data);

    void onDecoded(T value);
}
