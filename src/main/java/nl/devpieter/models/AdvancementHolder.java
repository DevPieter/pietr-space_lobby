package nl.devpieter.models;

import net.minestom.server.advancements.Advancement;
import org.jetbrains.annotations.Nullable;

public record AdvancementHolder(String id, @Nullable AdvancementHolder parent, Advancement advancement) {
}
