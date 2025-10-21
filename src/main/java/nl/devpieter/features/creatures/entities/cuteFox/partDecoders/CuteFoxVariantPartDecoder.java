package nl.devpieter.features.creatures.entities.cuteFox.partDecoders;

import nl.devpieter.features.creatures.entities.cuteFox.enums.CuteFoxVariant;
import nl.devpieter.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class CuteFoxVariantPartDecoder extends PartDecoder<CuteFoxVariant> {

    private final CuteFoxVariant fallback;

    public CuteFoxVariantPartDecoder(CuteFoxVariant fallback, Consumer<CuteFoxVariant> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        try {
            int variantId = Integer.parseInt(data);
            CuteFoxVariant variant = CuteFoxVariant.fromId(variantId);

            if (variant != null) onDecoded(variant);
            else onDecoded(fallback);
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
