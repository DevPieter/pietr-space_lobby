package nl.devpieter.features.creatures.entities.cuteCat.partDecoders;

import nl.devpieter.features.creatures.entities.cuteCat.enums.CuteCatCollar;
import nl.devpieter.features.signs.partDecoder.PartDecoder;

import java.util.function.Consumer;

public class CuteCatCollarPartDecoder extends PartDecoder<CuteCatCollar> {

    private final CuteCatCollar fallback;

    public CuteCatCollarPartDecoder(CuteCatCollar fallback, Consumer<CuteCatCollar> onDecodedCallback) {
        super(onDecodedCallback);
        this.fallback = fallback;
    }

    @Override
    public void decode(String data) {
        try {
            int variantId = Integer.parseInt(data);
            CuteCatCollar variant = CuteCatCollar.fromId(variantId);

            if (variant != null) onDecoded(variant);
            else onDecoded(fallback);
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
