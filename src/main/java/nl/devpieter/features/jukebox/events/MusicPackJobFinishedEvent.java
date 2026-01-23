package nl.devpieter.features.jukebox.events;

import nl.devpieter.features.jukebox.models.FinishedJobData;
import nl.devpieter.sees.event.SEvent;

import java.util.UUID;

public record MusicPackJobFinishedEvent(UUID jobId, FinishedJobData finishedJobData) implements SEvent {
}
