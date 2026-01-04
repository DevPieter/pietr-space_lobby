package nl.devpieter.features.resourcePack.models;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;

import java.net.URI;
import java.util.UUID;

public record ResourcePack(
        UUID packId,
        String url,
        String hash,
        Component displayName,
        Component promptMessage,
        boolean mainPack,
        boolean autoPrompt
) {
    public ResourcePackInfo toPackInfo() {
        return ResourcePackInfo.resourcePackInfo().id(packId()).uri(URI.create(url())).hash(hash()).build();
    }
}
