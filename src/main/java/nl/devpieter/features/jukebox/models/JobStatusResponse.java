package nl.devpieter.features.jukebox.models;

import nl.devpieter.features.jukebox.enums.MusicPackJobState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record JobStatusResponse(
        int state,
        @Nullable UUID finishedPackId,
        @Nullable String downloadUrl,
        @Nullable String errorMessage,
        List<ProcessedSong> processedSongs
) {
    public MusicPackJobState getJobState() {
        return MusicPackJobState.fromInt(state);
    }
}
