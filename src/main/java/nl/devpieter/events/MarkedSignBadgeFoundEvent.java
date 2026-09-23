package nl.devpieter.events;

import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.sees.event.SEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MarkedSignBadgeFoundEvent(
        @NotNull String key,
        @NotNull List<SignHolder> signHolders
) implements SEvent {
}
