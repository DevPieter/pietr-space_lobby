package nl.devpieter.features.mobs.decoder;

import nl.devpieter.features.mobs.decoder.partDecoder.IPartDecoder;

import java.util.HashMap;
import java.util.List;

public interface ISignDecodable {

    HashMap<Integer, List<IPartDecoder<?>>> getDecoders();

}
