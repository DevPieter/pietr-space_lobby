package space.pietr.lobby.common.models.holder.entity;

import net.minestom.server.coordinate.Pos;

public record EntityIdPosHolder(
        int entityId,
        Pos entityPos
) {
}
