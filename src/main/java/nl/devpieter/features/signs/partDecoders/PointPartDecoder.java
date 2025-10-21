package nl.devpieter.features.signs.partDecoders;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import nl.devpieter.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class PointPartDecoder extends PartDecoder<Point> {

    private final Point fallback;

    public PointPartDecoder(Point fallback, Consumer<Point> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        String[] parts = data.split(",");
        if (parts.length != 3) {
            onDecoded(fallback);
            return;
        }

        try {
            float x = Float.parseFloat(parts[0]);
            float y = Float.parseFloat(parts[1]);
            float z = Float.parseFloat(parts[2]);

            onDecoded(new Pos(x, y, z));
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
