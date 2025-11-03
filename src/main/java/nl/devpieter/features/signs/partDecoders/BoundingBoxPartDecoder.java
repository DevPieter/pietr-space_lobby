package nl.devpieter.features.signs.partDecoders;

import net.minestom.server.collision.BoundingBox;
import nl.devpieter.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class BoundingBoxPartDecoder extends PartDecoder<BoundingBox> {

    private final BoundingBox fallback;

    public BoundingBoxPartDecoder(Consumer<BoundingBox> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = new BoundingBox(1, 1, 1);
    }

    public BoundingBoxPartDecoder(BoundingBox fallback, Consumer<BoundingBox> onDecodedCallback) {
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
            float width = Float.parseFloat(parts[0]);
            float height = Float.parseFloat(parts[1]);
            float depth = Float.parseFloat(parts[2]);

            onDecoded(new BoundingBox(width, height, depth));
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
