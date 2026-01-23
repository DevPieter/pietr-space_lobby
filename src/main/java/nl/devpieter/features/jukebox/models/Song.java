package nl.devpieter.features.jukebox.models;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public record Song(
        @NotNull String title,
        @NotNull Key musicKey,
        long durationMillis
) {
    public @NotNull Sound createSound() {
        return Sound.sound(
                musicKey,
                Sound.Source.RECORD,
                1.0f,
                1.0f
        );
    }
}
