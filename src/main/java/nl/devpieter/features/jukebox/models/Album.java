package nl.devpieter.features.jukebox.models;

import java.util.List;

public record Album(
        String name,
        List<Song> songs
) {
}
