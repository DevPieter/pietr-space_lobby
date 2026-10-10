package space.pietr.lobby.common.models.holder.block;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public record IBlockHolder(
        Instance instance,
        BlockVec position,
        Block block
) {

    public IBlockHolder(Instance instance, Point position, Block block) {
        this(instance, new BlockVec(position.blockX(), position.blockY(), position.blockZ()), block);
    }
}
