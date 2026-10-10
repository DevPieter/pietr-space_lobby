package space.pietr.lobby.common.features.signs;

import space.pietr.lobby.common.features.signs.enums.SignMaterial;
import space.pietr.lobby.common.features.signs.enums.SignType;
import space.pietr.lobby.common.models.holder.block.IBlockHolder;

import java.util.List;

public record SignHolder(
        IBlockHolder block,
        SignType type,
        SignMaterial material,
        List<String> lines
) {
}
