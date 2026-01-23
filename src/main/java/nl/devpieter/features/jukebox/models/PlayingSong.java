package nl.devpieter.features.jukebox.models;

import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public record PlayingSong(@NotNull Song song, @NotNull Sound sound, long startTimeMs) {

    public static PlayingSong of(Song song) {
        return new PlayingSong(song, song.createSound(), System.currentTimeMillis());
    }
}
