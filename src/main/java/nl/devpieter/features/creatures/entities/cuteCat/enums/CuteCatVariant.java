package nl.devpieter.features.creatures.entities.cuteCat.enums;

import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CuteCatVariant {
    BLACK(0, CatVariant.BLACK),
    ALL_BLACK(1, CatVariant.ALL_BLACK),
    CALICO(2, CatVariant.CALICO),
    JELLIE(3, CatVariant.JELLIE),
    TABBY(4, CatVariant.TABBY),
    RAGDOLL(5, CatVariant.RAGDOLL),
    SIAMESE(6, CatVariant.SIAMESE),
    PERSIAN(7, CatVariant.PERSIAN),
    BRITISH_SHORTHAIR(8, CatVariant.BRITISH_SHORTHAIR),
    RED(9, CatVariant.RED),
    WHITE(10, CatVariant.WHITE);

    private final int id;
    private final RegistryKey<@NotNull CatVariant> variant;

    CuteCatVariant(int id, RegistryKey<@NotNull CatVariant> variant) {
        this.id = id;
        this.variant = variant;
    }

    public int getId() {
        return id;
    }

    public RegistryKey<@NotNull CatVariant> getVariant() {
        return variant;
    }

    public static @Nullable CuteCatVariant fromId(int id) {
        for (CuteCatVariant variant : values()) {
            if (variant.getId() == id) return variant;
        }

        return null;
    }
}
