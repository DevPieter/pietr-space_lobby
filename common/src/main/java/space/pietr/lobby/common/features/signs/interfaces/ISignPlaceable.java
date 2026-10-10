package space.pietr.lobby.common.features.signs.interfaces;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.pietr.lobby.common.features.signs.SignHolder;
import space.pietr.lobby.common.models.holder.block.IBlockChangeHolder;

import java.util.List;

public interface ISignPlaceable extends ISignDecodable {

    String getIdentifier();

    List<IBlockChangeHolder> place(Instance instance, Point point, SignHolder signHolder);

    default IBlockChangeHolder replace(Instance instance, Point point, Block newBlock) {
        IBlockChangeHolder changeHolder = new IBlockChangeHolder(
                instance,
                new BlockVec(point.blockX(), point.blockY(), point.blockZ()),
                instance.getBlock(point),
                newBlock
        );

        instance.setBlock(point, newBlock);
        return changeHolder;
    }
}
