package space.pietr.lobby.api.features.signs.events;

import nl.devpieter.sees.event.SEvent;
import space.pietr.lobby.common.features.signs.SignHolder;

import java.util.List;

public record MarkedSignBatchFoundEvent(
        String key,
        List<SignHolder> signHolders
) implements SEvent {
}
