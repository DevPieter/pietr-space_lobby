package nl.devpieter.features.jukebox.events;

import nl.devpieter.sees.event.SEvent;

import java.util.UUID;

public record MusicPackJobFailedEvent(UUID jobId) implements SEvent {
}
