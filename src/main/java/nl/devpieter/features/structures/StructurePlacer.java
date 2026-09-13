package nl.devpieter.features.structures;

import net.minestom.server.instance.Instance;
import nl.devpieter.features.structures.models.Structure;
import nl.devpieter.models.SimpleVec;

public class StructurePlacer {

//    https://minecraft.wiki/w/Structure_Block
//    Rotation (0, 90, 180, 270)
//    Sets the rotation of the structure to 0° (no rotation), 90° clockwise, 180° clockwise, and 270° clockwise (or 90° counter-clockwise).

//    Mirror (¦, <>, ^v)
//    Sets the mirroring of the structure to none (¦), left to right (< >), or front to back (^ v). At 0° rotation < > mirrors across the X-axis and ^ v mirrors across the Z-axis.​[more information needed]
//    In Bedrock Edition, the options are x and z, which can be toggled to set mirroring.

    public enum Rotation {
        ROTATE_0,
        ROTATE_90,
        ROTATE_180,
        ROTATE_270
    }

    public enum Mirror {
        NONE,
        LEFT_RIGHT,
        FRONT_BACK
    }

    public static void placeStructure(
            Instance instance,
            SimpleVec position,
            Structure structure,
            Rotation rotation,
            Mirror mirror
    ) {
        // TODO - Implement
    }
}
