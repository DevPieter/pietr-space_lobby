package nl.devpieter.utils;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public class ParticleUtils {

    public static void spawnParticleToViewersAndSelf(@NotNull Entity entity, @NotNull Particle particle, @NotNull Point position, @NotNull Point offset, float speed, int count) {
        ParticlePacket packet = new ParticlePacket(
                particle,
                position,
                offset,
                speed,
                count
        );

        entity.sendPacketToViewersAndSelf(packet);
    }

    public static void spawnParticleToViewers(@NotNull Entity entity, @NotNull Particle particle, @NotNull Point position, @NotNull Point offset, float speed, int count) {
        ParticlePacket packet = new ParticlePacket(
                particle,
                position,
                offset,
                speed,
                count
        );

        entity.sendPacketToViewers(packet);
    }
}
