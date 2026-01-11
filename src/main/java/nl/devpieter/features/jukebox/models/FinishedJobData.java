package nl.devpieter.features.jukebox.models;

import java.util.List;

public record FinishedJobData(
        String downloadUrl,
        List<ProcessedSong> songs
) {
}
