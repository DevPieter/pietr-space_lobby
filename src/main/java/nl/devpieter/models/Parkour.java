package nl.devpieter.models;

import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.UUID;

public record Parkour(
        UUID uuid,
        UUID worldUuid,
        Pos start,
        Pos end,
        List<Pos> checkpoints,
        Pos leaderboardPos,
        List<PlayerParkourStat> leaderboard
) {
}
