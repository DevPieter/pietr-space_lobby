package nl.devpieter.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class WorldUtils {

    public static void placeBoundingBox(@NotNull InstanceContainer instance, @NotNull Pos origin, @NotNull BoundingBox box) {
        double sizeX = Math.abs(box.relativeEnd().x() - box.relativeStart().x());
        double sizeY = Math.abs(box.relativeEnd().y() - box.relativeStart().y());
        double sizeZ = Math.abs(box.relativeEnd().z() - box.relativeStart().z());

        double minX = Math.min(box.relativeStart().x(), box.relativeEnd().x());
        double minY = Math.min(box.relativeStart().y(), box.relativeEnd().y());
        double minZ = Math.min(box.relativeStart().z(), box.relativeEnd().z());

        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setInstance(instance, origin.add(minX, minY, minZ));

        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.GLASS);
            meta.setScale(new Vec(sizeX, sizeY, sizeZ));

            meta.setHasNoGravity(true);
        });
    }

    public static Entity placeMarker(@NotNull InstanceContainer instance, @NotNull Point point, @NotNull Component text) {
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        entity.setInstance(instance, point);

        entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
            textDisplayMeta.setText(text);
            textDisplayMeta.setScale(new Vec(0.5, 0.5, 0.5));

            textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
            textDisplayMeta.setHasNoGravity(true);
        });

        return entity;
    }
}
