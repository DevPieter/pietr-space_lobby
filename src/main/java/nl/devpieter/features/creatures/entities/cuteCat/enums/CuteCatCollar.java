package nl.devpieter.features.creatures.entities.cuteCat.enums;

import net.minestom.server.color.DyeColor;

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

    public DyeColor getColor() {
        return color;
    }

    public static CuteCatCollar fromId(int id) {
        for (CuteCatCollar collar : values()) {
            if (collar.id == id) return collar;
        }

        return RED;
    }
}
