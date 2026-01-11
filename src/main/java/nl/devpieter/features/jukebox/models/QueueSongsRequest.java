package nl.devpieter.features.jukebox.models;

import java.util.List;

public record QueueSongsRequest(List<String> trackUrls) {
}
