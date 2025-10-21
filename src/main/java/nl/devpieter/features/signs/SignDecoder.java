package nl.devpieter.features.signs;

import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class SignDecoder {

    public static final String PART_DELIMITER = "`";

    public static void decodeLines(@NotNull List<String> lines, @NotNull HashMap<Integer, List<IPartDecoder<?>>> decoders) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(PART_DELIMITER);

            List<IPartDecoder<?>> partDecoder = decoders.get(i + 1);
            if (partDecoder == null) continue;

            for (int j = 0; j < parts.length; j++) {
                if (j >= partDecoder.size()) continue;
                partDecoder.get(j).decode(parts[j]);
            }
        }
    }
}
