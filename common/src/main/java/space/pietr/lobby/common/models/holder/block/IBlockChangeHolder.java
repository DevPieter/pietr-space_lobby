package space.pietr.lobby.common.models.holder.block;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public record IBlockChangeHolder(
        Instance instance,
        BlockVec position,
        Block original,
        Block replacement
) {
}
