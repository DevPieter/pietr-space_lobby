package space.pietr.lobby.common.features.signs.partDecoder;

public interface IPartDecoder<T> {

    void decode(String data);

    void onDecoded(T value);
}
