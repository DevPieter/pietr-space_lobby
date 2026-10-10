package space.pietr.lobby.common.models.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import space.pietr.lobby.common.models.holder.block.IBlockHolder;

public record IPointHolder(
        Instance instance,
        Point position
) {

    public static IPointHolder of(IBlockHolder block) {
        return new IPointHolder(block.instance(), block.position());
    }
}
