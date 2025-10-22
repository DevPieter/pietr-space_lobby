package nl.devpieter.features.farming.crops.genericCrop.partDecoders;

import nl.devpieter.features.farming.crops.genericCrop.enums.GenericCrop;
import nl.devpieter.features.signs.partDecoder.PartDecoder;

import java.util.Random;
import java.util.function.Consumer;

public class GenericCropPartDecoder extends PartDecoder<GenericCrop> {

    private final GenericCrop fallback;

    public GenericCropPartDecoder(Consumer<GenericCrop> onDecodedCallback) {
        super(onDecodedCallback);

        Random random = new Random();
        GenericCrop[] crops = GenericCrop.values();
        this.fallback = crops[random.nextInt(crops.length)];
    }

    @Override
    public void decode(String data) {
        try {
            GenericCrop variant = GenericCrop.fromName(data);

            if (variant != null) onDecoded(variant);
            else onDecoded(fallback);
        } catch (NumberFormatException e) {
            onDecoded(fallback);
        }
    }
}
