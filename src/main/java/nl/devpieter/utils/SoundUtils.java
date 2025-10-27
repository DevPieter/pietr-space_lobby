package nl.devpieter.utils;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public class SoundUtils {

    public static void playSoundInWorld(@NotNull Instance instance, @NotNull Point point, @NotNull Sound sound) {
        instance.playSound(sound, point);
    }

    public static void playSoundToViewers(@NotNull Entity entity, @NotNull Point point, @NotNull Sound sound) {
        SoundEvent soundEvent = SoundEvent.fromKey(sound.name());
        if (soundEvent == null) return;

        SoundEffectPacket packet = new SoundEffectPacket(
                soundEvent,
                sound.source(),
                (int) point.x(),
                (int) point.y(),
                (int) point.z(),
                sound.volume(),
                sound.pitch(),
                0L
        );

        entity.sendPacketToViewers(packet);
    }
}
