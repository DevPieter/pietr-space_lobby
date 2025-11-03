package nl.devpieter.features.mining.models;

import nl.devpieter.models.BlockHolder;

public record MiningHolder(BlockHolder holder, long startTime, long totalBreakTicks) {
}
