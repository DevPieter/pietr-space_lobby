package nl.devpieter.features.jukebox.models;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ProcessedSong(
        UUID id,
        UUID musicPackJobId,
        int state,
        String originalUrl,
        @Nullable String fileName,
        @Nullable String title,
        @Nullable String duration,
        int failureReason,
        @Nullable String errorMessage,
        String createdAt
) {
}
