package nl.devpieter.features.farming.crops.sugarCane;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.farming.ISignPlaceable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.models.BlockChangeHolder;

import java.util.HashMap;
import java.util.List;

public class SugarCanePlaceable implements ISignPlaceable {

    @Override
    public String getIdentifier() {
        return "SugarCane";
    }

    @Override
    public List<BlockChangeHolder> place(Instance instance, Point point, SignHolder signHolder) {
        Block berryBush = Block.SUGAR_CANE.withHandler(new SugarCaneHandler(point));
        return List.of(replace(instance, point, berryBush));
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>();
    }
}
