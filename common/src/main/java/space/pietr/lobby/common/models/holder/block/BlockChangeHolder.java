package space.pietr.lobby.common.models.holder.block;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;

public record BlockChangeHolder(
        BlockVec position,
        Block original,
        Block replacement
) {
}
