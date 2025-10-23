package nl.devpieter.features.signs.interfaces;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.models.BlockChangeHolder;
import nl.devpieter.models.BlockHolder;

import java.util.List;

public interface ISignPlaceable extends ISignDecodable {

    String getIdentifier();

    List<BlockChangeHolder> place(Instance instance, Point point, SignHolder signHolder);

    default BlockChangeHolder replace(Instance instance, Point point, Block newBlock) {
        BlockChangeHolder changeHolder = new BlockChangeHolder(
                instance,
                new BlockHolder(
                        instance,
                        point,
                        instance.getBlock(point)
                ),
                new BlockHolder(
                        instance,
                        point,
                        newBlock
                )
        );

        instance.setBlock(point, newBlock);
        return changeHolder;
    }
}
