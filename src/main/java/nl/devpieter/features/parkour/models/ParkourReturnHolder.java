package nl.devpieter.features.parkour.models;

import net.minestom.server.timer.Task;

public record ParkourReturnHolder(String parkourId, Task expirationTask) {
}
