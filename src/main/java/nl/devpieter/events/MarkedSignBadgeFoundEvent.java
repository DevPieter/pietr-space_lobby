package nl.devpieter.events;

import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.sees.event.SEvent;

import java.util.List;

public record MarkedSignBadgeFoundEvent(
        String key,
        List<SignHolder> signHolders
) implements SEvent {
}
