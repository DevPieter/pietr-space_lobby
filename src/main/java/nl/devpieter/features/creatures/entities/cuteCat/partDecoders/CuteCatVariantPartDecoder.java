package nl.devpieter.features.creatures.entities.cuteCat.partDecoders;

import nl.devpieter.features.creatures.entities.cuteCat.enums.CuteCatVariant;
import space.pietr.lobby.common.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class CuteCatVariantPartDecoder extends PartDecoder<CuteCatVariant> {

    private final CuteCatVariant fallback;

    public CuteCatVariantPartDecoder(CuteCatVariant fallback, Consumer<CuteCatVariant> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        try {
            int variantId = Integer.parseInt(data);
            CuteCatVariant variant = CuteCatVariant.fromId(variantId);

            if (variant != null) onDecoded(variant);
            else onDecoded(fallback);
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
