package nl.devpieter.commands;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.entities.CuteCat;
import nl.devpieter.utils.WorldUtils;

import java.util.List;
import java.util.UUID;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /test");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Instance instance = player.getInstance();

            WorldUtils.findBlocks(instance, new Vec(0, 64, 0), 24, Block.OAK_SIGN).forEach(position -> {
                check(instance, position);
            });
        });
    }

    private void check(Instance instance, Point point) {
        Block block = instance.getBlock(point);
        if (!block.compare(Block.OAK_SIGN) || !block.hasNbt()) return;

        CompoundBinaryTag nbt = block.nbt();
        if (nbt == null) return;

        CompoundBinaryTag fText = nbt.getCompound("front_text");
        ListBinaryTag fMessages = fText.getList("messages", BinaryTagTypes.STRING);

        List<String> lines = fMessages.stream()
                .map(tag -> ((StringBinaryTag) tag).value())
                .toList();

        String firstLine = lines.getFirst();
        String secondLine = lines.size() > 1 ? lines.get(1) : "";

        int rotation = Integer.parseInt(block.getProperty("rotation"));
        SignRotation signRotation = SignRotation.fromValue(rotation);

        if (firstLine.equals("CuteCat")) {
            instance.setBlock(point, Block.AIR);

            // [variant],[collar],[lying]
            // [int],[int],[t|f]

            String[] parts = secondLine.split(",");
            CuteCat.CuteCatVariant variant = CuteCat.CuteCatVariant.BLACK;
            CuteCat.CuteCatCollar collar = CuteCat.CuteCatCollar.RED;
            boolean lying = false;

            if (parts.length >= 1) {
                int variantId = Integer.parseInt(parts[0]);
                for (CuteCat.CuteCatVariant v : CuteCat.CuteCatVariant.values()) {
                    if (v.getId() == variantId) {
                        variant = v;
                        break;
                    }
                }
            }

            if (parts.length >= 2) {
                int collarId = Integer.parseInt(parts[1]);
                for (CuteCat.CuteCatCollar c : CuteCat.CuteCatCollar.values()) {
                    if (c.getId() == collarId) {
                        collar = c;
                        break;
                    }
                }
            }

            if (parts.length >= 3) {
                lying = parts[2].equalsIgnoreCase("t");
            }

            new CuteCat(variant, collar, lying).setInstance(instance, new Pos(
                    point.x() + 0.5,
                    point.y(),
                    point.z() + 0.5,
                    signRotation.getYaw(),
                    0
            ));
        }
    }

    enum SignRotation {
        SOUTH(0),
        SOUTH_SOUTHWEST(1),
        SOUTHWEST(2),
        WEST_SOUTHWEST(3),
        WEST(4),
        WEST_NORTHWEST(5),
        NORTHWEST(6),
        NORTH_NORTHWEST(7),
        NORTH(8),
        NORTH_NORTHEAST(9),
        NORTHEAST(10),
        EAST_NORTHEAST(11),
        EAST(12),
        EAST_SOUTHEAST(13),
        SOUTHEAST(14),
        SOUTH_SOUTHEAST(15);

        private final int value;
        private final float yaw;

        SignRotation(int value) {
            this.value = value;
            this.yaw = (value * 22.5f);
        }

        public int getValue() {
            return value;
        }

        public float getYaw() {
            return yaw;
        }

        public static SignRotation fromValue(int value) {
            for (SignRotation rotation : values()) {
                if (rotation.getValue() == value) return rotation;
            }

            return SOUTH;
        }
    }
}