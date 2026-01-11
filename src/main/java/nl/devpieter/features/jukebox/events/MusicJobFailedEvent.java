package nl.devpieter.features.jukebox.events;

import nl.devpieter.sees.event.SEvent;

import java.util.UUID;

public record MusicJobFailedEvent(UUID jobId) implements SEvent {
}
