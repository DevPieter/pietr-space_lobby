package nl.devpieter.models;

import net.minestom.server.timer.Task;

public record TaskRunnable<T extends Runnable>(Task task, T runnable) {
}
