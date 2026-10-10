package space.pietr.lobby.common.features.signs.interfaces;

import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;

import java.util.HashMap;
import java.util.List;

public interface ISignDecodable {

    HashMap<Integer, List<IPartDecoder<?>>> getDecoders();

    default ISignDecodable freshInstance() {
        try {
            return this.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create fresh instance of " + this.getClass().getName(), e);
        }
    }
}
