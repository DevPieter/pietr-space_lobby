package nl.devpieter.features.jukebox.models.old;

import nl.devpieter.features.resourcePack.models.ResourcePack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Album(
        String name,
        List<Song> songs,
        @Nullable ResourcePack resourcePack
) {
}
