package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.utils.minestom.MineMathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BedBlockHandler implements BlockHandler {

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "bed");
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.NBT("occupied")
        );
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (!(interaction.getPlayer() instanceof LobbyPlayer player)) return false;

        Block block = interaction.getBlock();

        String facingProperty = block.getProperty("facing");
        if (facingProperty == null) facingProperty = "north";

        Direction facing = MineMathUtils.getFacingDirection(facingProperty);
        boolean isHeadPart = block.getProperty("part") != null && "head".equals(block.getProperty("part"));

        Point bedPosition = isHeadPart ? interaction.getBlockPosition().sub(facing.normalX(), 0, facing.normalZ()) : interaction.getBlockPosition();
        Point headPartPosition = getHeadPartPosition(bedPosition, facing);

        player.enterBed(headPartPosition);

        return true;
    }

    private Point getHeadPartPosition(Point bedPosition, Direction facing) {
        return new Vec(bedPosition.x() + facing.normalX(), bedPosition.y(), bedPosition.z() + facing.normalZ());
    }
}
