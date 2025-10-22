package nl.devpieter.features.farming.crops.genericCrop;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.farming.ISignPlaceable;
import nl.devpieter.features.farming.crops.genericCrop.enums.GenericCrop;
import nl.devpieter.features.farming.crops.genericCrop.partDecoders.GenericCropPartDecoder;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import nl.devpieter.models.BlockChangeHolder;

import java.util.HashMap;
import java.util.List;

public class GenericCropPlaceable implements ISignPlaceable {

    private GenericCrop crop = GenericCrop.WHEAT;

    @Override
    public String getIdentifier() {
        return "GenericCrop";
    }

    @Override
    public List<BlockChangeHolder> place(Instance instance, Point point, SignHolder signHolder) {
        Block cropBlock = crop.getBlock().withProperty("age", String.valueOf(crop.getMaxGrowthStage()))
                .withHandler(new GenericCropHandler(crop));

        Block farmLand = Block.FARMLAND.withProperty("moisture", "7");

        return List.of(
                replace(instance, point, cropBlock),
                replace(instance, point.sub(0, 1, 0), farmLand)
        );
    }

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(1, List.of(
                    new GenericCropPartDecoder(v -> crop = v)
            ));
        }};
    }
}
