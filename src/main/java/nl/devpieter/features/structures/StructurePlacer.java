package nl.devpieter.features.structures;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.structures.models.Structure;
import nl.devpieter.features.structures.models.nbt.StructureBlock;
import nl.devpieter.features.structures.models.nbt.StructurePalette;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructurePlacer {

    // TODO - Implement, refactor this AI slop

    private static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");

    public enum Rotation {
        ROTATE_0,
        ROTATE_90,
        ROTATE_180,
        ROTATE_270
    }

    public enum Mirror {
        NONE,
        // Reflect across the X axis (negate Z), before rotating.
        LEFT_RIGHT,
        // Reflect across the Z axis (negate X), before rotating.
        FRONT_BACK
    }

    /**
     * Places blocks and block-entity NBT synchronously; saved entities are ignored.
     * The floored position is the origin/pivot. Mirroring precedes clockwise rotation,
     * so transformed blocks can extend into negative offsets from the origin.
     * Chunks must be loaded, or the instance must have automatic chunk loading enabled.
     * Air replaces existing blocks; structure void leaves existing blocks untouched.
     *
     * @throws IllegalArgumentException if the palette or a block's state index is invalid
     */
    public static void placeStructure(
            Instance instance,
            BlockVec origin,
            Structure structure,
            Rotation rotation,
            Mirror mirror
    ) {
        // Resolve and validate before changing the world, and transform each palette entry only once.
        List<Block> palette = structure.nbt().palette().stream()
                .map(entry -> transformBlock(readPaletteBlock(entry), rotation, mirror))
                .toList();
        for (StructureBlock entry : structure.nbt().blocks()) {
            if (entry.state() < 0 || entry.state() >= palette.size()) {
                throw new IllegalArgumentException("Invalid palette index " + entry.state() + " in structure " + structure.id());
            }
        }

        for (StructureBlock entry : structure.nbt().blocks()) {
            Block block = palette.get(entry.state());
            if (block.id() == Block.STRUCTURE_VOID.id()) continue;

            Vec offset = transformPosition(entry.pos().x(), entry.pos().y(), entry.pos().z(), rotation, mirror);
            int x = origin.blockX() + offset.blockX();
            int y = origin.blockY() + offset.blockY();
            int z = origin.blockZ() + offset.blockZ();

            CompoundBinaryTag nbt = entry.nbt();
            if (nbt != null && !nbt.keySet().isEmpty()) {
                block = block.withNbt(nbt.putInt("x", x).putInt("y", y).putInt("z", z));
            }

            // Preserve the saved states instead of recalculating neighbors during placement.
            instance.setBlock(x, y, z, block, false);
        }
    }

    private static Block readPaletteBlock(StructurePalette entry) {
        Block block = Block.fromKey(entry.name());
        if (block == null) throw new IllegalArgumentException("Unknown structure block: " + entry.name());

        if (entry.properties() != null) {
            for (String property : entry.properties().keySet()) {
                block = block.withProperty(property, entry.properties().getString(property));
            }
        }
        return block;
    }

    private static Vec transformPosition(int x, int y, int z, Rotation rotation, Mirror mirror) {
        if (mirror == Mirror.LEFT_RIGHT) z = -z;
        if (mirror == Mirror.FRONT_BACK) x = -x;
        return switch (rotation) {
            case ROTATE_0 -> new Vec(x, y, z);
            case ROTATE_90 -> new Vec(-z, y, x);
            case ROTATE_180 -> new Vec(-x, y, -z);
            case ROTATE_270 -> new Vec(z, y, -x);
        };
    }

    private static Block transformBlock(Block block, Rotation rotation, Mirror mirror) {
        Map<String, String> properties = new HashMap<>(block.properties());
        for (var entry : block.properties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
                case "facing" -> properties.put(key, transformDirection(value, rotation, mirror));
                case "axis" -> {
                    if (rotation == Rotation.ROTATE_90 || rotation == Rotation.ROTATE_270) {
                        properties.put(key, switch (value) {
                            case "x" -> "z";
                            case "z" -> "x";
                            default -> value;
                        });
                    }
                }
                case "rotation" -> {
                    int angle = Integer.parseInt(value);
                    if (mirror == Mirror.LEFT_RIGHT) angle = 8 - angle;
                    if (mirror == Mirror.FRONT_BACK) angle = -angle;
                    properties.put(key, Integer.toString(Math.floorMod(angle + rotation.ordinal() * 4, 16)));
                }
                case "orientation" -> {
                    // Jigsaw orientation contains two directions, including a possible vertical one.
                    String[] directions = value.split("_");
                    for (int i = 0; i < directions.length; i++) {
                        directions[i] = transformDirection(directions[i], rotation, mirror);
                    }
                    properties.put(key, String.join("_", directions));
                }
                case "shape" -> properties.put(key, transformShape(value, rotation, mirror));
                case "hinge", "type" -> {
                    if (mirror != Mirror.NONE) properties.put(key, swapHandedness(value));
                }
                case "north", "east", "south", "west" ->
                    // Connections on fences, walls, redstone, vines, etc. move with the block.
                        properties.put(transformDirection(key, rotation, mirror), value);
                default -> {
                }
            }
        }
        return block.withProperties(properties);
    }

    private static String transformDirection(String direction, Rotation rotation, Mirror mirror) {
        int index = DIRECTIONS.indexOf(direction);
        if (index < 0) return direction;
        if (mirror == Mirror.LEFT_RIGHT && index % 2 == 0) index += 2;
        if (mirror == Mirror.FRONT_BACK && index % 2 == 1) index += 2;
        return DIRECTIONS.get((index + rotation.ordinal()) % 4);
    }

    private static String transformShape(String shape, Rotation rotation, Mirror mirror) {
        if (shape.startsWith("ascending_")) {
            return "ascending_" + transformDirection(shape.substring(10), rotation, mirror);
        }
        if (shape.startsWith("inner_") || shape.startsWith("outer_")) {
            return mirror == Mirror.NONE ? shape : swapHandedness(shape);
        }
        String[] ends = shape.split("_");
        if (ends.length != 2 || !DIRECTIONS.contains(ends[0]) || !DIRECTIONS.contains(ends[1])) return shape;

        String first = transformDirection(ends[0], rotation, mirror);
        String second = transformDirection(ends[1], rotation, mirror);
        // Rail shapes have canonical names; e.g. east_north is spelled north_east.
        if (opposite(first).equals(second)) {
            return first.equals("north") || first.equals("south") ? "north_south" : "east_west";
        }
        return first.equals("north") || first.equals("south") ? first + "_" + second : second + "_" + first;
    }

    private static String opposite(String direction) {
        return DIRECTIONS.get((DIRECTIONS.indexOf(direction) + 2) % 4);
    }

    private static String swapHandedness(String value) {
        if (value.endsWith("left")) return value.substring(0, value.length() - 4) + "right";
        if (value.endsWith("right")) return value.substring(0, value.length() - 5) + "left";
        return value;
    }
}
