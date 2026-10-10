package space.pietr.lobby.common.utils.minestom;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundUtils {

    public static void playSoundForPlayer(@NotNull Player player, @NotNull Sound.Type type) {
        playSoundForPlayer(player, type, Sound.Source.MASTER, 1.0f, 1.0f);
    }

    public static void playSoundForPlayer(@NotNull Player player, @NotNull Sound.Type type, float volume, float pitch) {
        playSoundForPlayer(player, type, Sound.Source.MASTER, volume, pitch);
    }

    public static void playSoundForPlayer(@NotNull Player player, @NotNull Sound.Type type, @NotNull Sound.Source source, float volume, float pitch) {
        playSoundForPlayer(player, Sound.sound(type, source, volume, pitch));
    }

    public static void playSoundForPlayer(@NotNull Player player, @Nullable Sound sound) {
        if (sound == null) return;
        player.playSound(sound, Sound.Emitter.self());
    }

    public static void playSoundInWorld(@NotNull Instance instance, @NotNull Point point, @NotNull Sound.Type type, float volume, float pitch) {
        playSoundInWorld(instance, point, type, Sound.Source.MASTER, volume, pitch);
    }

    public static void playSoundInWorld(@NotNull Instance instance, @NotNull Point point, @NotNull Sound.Type type, @NotNull Sound.Source source, float volume, float pitch) {
        playSoundInWorld(instance, point, Sound.sound(type, source, volume, pitch));
    }

    public static void playSoundInWorld(@NotNull Instance instance, @NotNull Point point, @Nullable Sound sound) {
        if (sound == null) return;
        instance.playSound(sound, point);
    }

    public static void playSoundToViewers(@NotNull Entity entity, @NotNull Sound.Type type, float volume, float pitch) {
        playSoundToViewers(entity, type, Sound.Source.MASTER, volume, pitch);
    }

    public static void playSoundToViewers(@NotNull Entity entity, @NotNull Sound.Type type, @NotNull Sound.Source source, float volume, float pitch) {
        playSoundToViewers(entity, type, source, volume, pitch);
    }

    public static void playSoundToViewers(@NotNull Entity entity, @NotNull Point point, @Nullable Sound sound) {
        if (sound == null) return;

        SoundEvent soundEvent = SoundEvent.fromKey(sound.name());
        if (soundEvent == null) return;

        SoundEffectPacket packet = new SoundEffectPacket(
                soundEvent,
                sound.source(),
                new Vec(point.x(), point.y(), point.z()),
                sound.volume(),
                sound.pitch(),
                0L
        );

        entity.sendPacketToViewers(packet);
    }
}
