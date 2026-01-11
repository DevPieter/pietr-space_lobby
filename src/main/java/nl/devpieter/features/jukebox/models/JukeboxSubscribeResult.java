package nl.devpieter.features.jukebox.models;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record JukeboxSubscribeResult(
        UUID playerId,
        @Nullable UUID previousJukeboxId,
        UUID newJukeboxId
) {
}
