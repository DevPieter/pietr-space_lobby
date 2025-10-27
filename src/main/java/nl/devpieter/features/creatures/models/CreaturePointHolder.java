package nl.devpieter.features.creatures.models;

import nl.devpieter.features.signs.interfaces.ISignSpawnable;
import nl.devpieter.models.PointHolder;

public record CreaturePointHolder(ISignSpawnable spawnable, PointHolder point) {
}
