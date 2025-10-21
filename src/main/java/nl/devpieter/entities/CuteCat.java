package nl.devpieter.entities;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

public class CuteCat extends EntityCreature {

    public CuteCat(CuteCatVariant variant, CuteCatCollar collar, boolean lying, boolean baby) {
        super(EntityType.CAT);

        CatMeta catMeta = (CatMeta) getEntityMeta();
        catMeta.setTamed(true);
        catMeta.setRelaxed(true);

        catMeta.setLying(lying);
        catMeta.setBaby(baby);

        set(DataComponents.CAT_COLLAR, collar.getColor());
        set(DataComponents.CAT_VARIANT, variant.getVariant());
    }

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

        public static CuteCatVariant fromId(int id) {
            for (CuteCatVariant variant : values()) {
                if (variant.getId() == id) return variant;
            }

            return BLACK;
        }
    }

    public enum CuteCatCollar {
        RED(0, DyeColor.RED),
        BLUE(1, DyeColor.BLUE),
        GREEN(2, DyeColor.GREEN),
        YELLOW(3, DyeColor.YELLOW),
        PURPLE(4, DyeColor.PURPLE),
        CYAN(5, DyeColor.CYAN),
        ORANGE(6, DyeColor.ORANGE),
        PINK(7, DyeColor.PINK),
        LIGHT_BLUE(8, DyeColor.LIGHT_BLUE),
        LIME(9, DyeColor.LIME),
        GRAY(10, DyeColor.GRAY),
        LIGHT_GRAY(11, DyeColor.LIGHT_GRAY),
        BROWN(12, DyeColor.BROWN),
        BLACK(13, DyeColor.BLACK),
        WHITE(14, DyeColor.WHITE);

        private final int id;
        private final DyeColor color;

        CuteCatCollar(int id, DyeColor color) {
            this.id = id;
            this.color = color;
        }

        public int getId() {
            return id;
        }

        public DyeColor getColor() {
            return color;
        }

        public static CuteCatCollar fromId(int id) {
            for (CuteCatCollar collar : values()) {
                if (collar.getId() == id) return collar;
            }

            return RED;
        }
    }
}
