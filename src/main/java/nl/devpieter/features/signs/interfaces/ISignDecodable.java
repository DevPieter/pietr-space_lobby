package nl.devpieter.features.signs.interfaces;

import nl.devpieter.features.signs.partDecoder.IPartDecoder;

import java.util.HashMap;
import java.util.List;

public interface ISignDecodable {

    HashMap<Integer, List<IPartDecoder<?>>> getDecoders();

}
