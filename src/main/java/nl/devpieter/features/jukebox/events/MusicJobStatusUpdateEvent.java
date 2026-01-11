package nl.devpieter.features.jukebox.events;

import nl.devpieter.features.jukebox.enums.MusicPackJobState;
import nl.devpieter.sees.event.SEvent;

import java.util.UUID;

public record MusicJobStatusUpdateEvent(UUID jobId, MusicPackJobState status) implements SEvent {
}
