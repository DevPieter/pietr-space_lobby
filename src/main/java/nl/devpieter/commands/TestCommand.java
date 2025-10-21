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
        String thirdLine = lines.size() > 2 ? lines.get(2) : "";

        if (firstLine.equals("CuteCat")) {
            instance.setBlock(point, Block.AIR);

            String[] firstParts = secondLine.split(",");
            String[] secondParts = thirdLine.split(",");

            CuteCat.CuteCatVariant variant = CuteCat.CuteCatVariant.BLACK;
            CuteCat.CuteCatCollar collar = CuteCat.CuteCatCollar.RED;
            boolean lying = false;
            boolean baby = false;

            float yaw = 0f;
            float pitch = 0f;
            float offsetX = 0f;
            float offsetZ = 0f;

            if (firstParts.length >= 1) {
                int variantId = Integer.parseInt(firstParts[0]);
                variant = CuteCat.CuteCatVariant.fromId(variantId);
            }

            if (firstParts.length >= 2) {
                int collarId = Integer.parseInt(firstParts[1]);
                collar = CuteCat.CuteCatCollar.fromId(collarId);
            }

            if (firstParts.length >= 3) {
                lying = firstParts[2].equalsIgnoreCase("t");
            }

            if (firstParts.length >= 4) {
                baby = firstParts[3].equalsIgnoreCase("t");
            }

            if (secondParts.length >= 1) {
                yaw = Float.parseFloat(secondParts[0]);
            }

            if (secondParts.length >= 2) {
                pitch = Float.parseFloat(secondParts[1]);
            }

            if (secondParts.length >= 3) {
                offsetX = Float.parseFloat(secondParts[2]);
            }

            if (secondParts.length >= 4) {
                offsetZ = Float.parseFloat(secondParts[3]);
            }

            new CuteCat(variant, collar, lying, baby).setInstance(instance, new Pos(
                    point.x() + 0.5 + offsetX,
                    point.y(),
                    point.z() + 0.5 + offsetZ,
                    yaw,
                    pitch
            ));
        }
    }
}