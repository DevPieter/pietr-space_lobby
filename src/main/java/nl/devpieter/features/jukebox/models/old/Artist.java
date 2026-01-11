package nl.devpieter.features.jukebox.models.old;

import java.util.List;

public record Artist(
        String name,
        List<Album> albums
) {
}
