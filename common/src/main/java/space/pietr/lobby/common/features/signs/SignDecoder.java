package space.pietr.lobby.common.features.signs;

import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.features.signs.partDecoder.IPartDecoder;

import java.util.HashMap;
import java.util.List;

public final class SignDecoder {

    public static final String PART_DELIMITER = "`";

    private SignDecoder() {
    }

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
