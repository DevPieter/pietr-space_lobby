package nl.devpieter.features.creatures.entities.cuteFox.enums;

import net.minestom.server.entity.metadata.animal.FoxMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CuteFoxVariant {
    RED(0, FoxMeta.Variant.RED),
    SNOW(1, FoxMeta.Variant.SNOW);

    private final int id;
    private final FoxMeta.@NotNull Variant variant;

    CuteFoxVariant(int id, FoxMeta.@NotNull Variant variant) {
        this.id = id;
        this.variant = variant;
    }

    public FoxMeta.@NotNull Variant getVariant() {
        return variant;
    }

    public static @Nullable CuteFoxVariant fromId(int id) {
        for (CuteFoxVariant variant : values()) {
            if (variant.id == id) return variant;
        }

        return null;
    }
}
