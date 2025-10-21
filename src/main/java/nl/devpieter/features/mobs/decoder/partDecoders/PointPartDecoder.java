package nl.devpieter.features.mobs.decoder.partDecoders;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import nl.devpieter.features.mobs.decoder.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class PointPartDecoder extends PartDecoder<Point> {

    public PointPartDecoder(Consumer<Point> onDecodedCallback) {
        super(onDecodedCallback);
    }

    @Override
    public void decode(String data) {
        String[] parts = data.split(",");
        if (parts.length != 3) throw new IllegalArgumentException("Invalid point data: " + data);

        try {
            float x = Float.parseFloat(parts[0]);
            float y = Float.parseFloat(parts[1]);
            float z = Float.parseFloat(parts[2]);

            onDecoded(new Pos(x, y, z));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in point data: " + data, e);
        }
    }
}
