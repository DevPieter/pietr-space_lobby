package nl.devpieter.features.mobs;

import net.minestom.server.coordinate.Point;
import nl.devpieter.features.mobs.decoder.ISignDecodable;
import nl.devpieter.features.mobs.decoder.partDecoder.IPartDecoder;
import nl.devpieter.features.mobs.decoder.partDecoders.FloatPartDecoder;
import nl.devpieter.features.mobs.decoder.partDecoders.IntPartDecoder;
import nl.devpieter.features.mobs.decoder.partDecoders.PointPartDecoder;
import nl.devpieter.features.mobs.decoder.partDecoders.StringPartDecoder;

import java.util.HashMap;
import java.util.List;

public class TestMob implements ISignDecodable {

    // Line 1
    private String name;

    // Line 2
    private int health;
    private int maxHealth;

    // Line 3
    private float x;
    private float y;
    private float z;

    // Line 4
    private Point position;

    @Override
    public HashMap<Integer, List<IPartDecoder<?>>> getDecoders() {
        return new HashMap<>() {{
            put(1, List.of(new StringPartDecoder(s -> name = s)));
            put(2, List.of(new IntPartDecoder(i -> health = i), new IntPartDecoder(i -> maxHealth = i)));
            put(3, List.of(new FloatPartDecoder(f -> x = f), new FloatPartDecoder(f -> y = f), new FloatPartDecoder(f -> z = f)));
            put(4, List.of(new PointPartDecoder(p -> position = p)));
        }};
    }

    public void testDecoding() {
        List<String> signLines = List.of(
                "Zombie",
                "20|40",
                "1.5|2|3.5",
                "10,64,10"
        );

        for (int i = 0; i < signLines.size(); i++) {
            String line = signLines.get(i);
            String[] parts = line.split("\\|");
            List<IPartDecoder<?>> decoders = getDecoders().get(i + 1);

            for (int j = 0; j < parts.length; j++) {
                if (j < decoders.size()) {
                    decoders.get(j).decode(parts[j]);
                }
            }
        }
    }

    public void printInfo() {
        System.out.println("Name: " + name);
        System.out.println("Health: " + health + "/" + maxHealth);
        System.out.println("XYZ: (" + x + ", " + y + ", " + z + ")");
        System.out.println("Position: " + position);
    }
}
