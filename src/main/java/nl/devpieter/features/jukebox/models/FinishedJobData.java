package nl.devpieter.features.jukebox.models;

import java.util.List;
import java.util.UUID;

public record FinishedJobData(
        UUID packId,
        String downloadUrl,
        List<ProcessedSong> songs
) {
}
