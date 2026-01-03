package nl.devpieter.features.jukebox.models;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.thread.Acquirable;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.minestom.ParticleUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

public class JukeboxState {

    private final Queue<Song> songQueue = new LinkedList<>();
    private final Acquirable<@NotNull ? extends Entity> displayAcquirable;

    private PlayingSong currentSong;

    private long lastDisplayTimeMs = 0;
    private long lastParticleTickMs = 0;

    public JukeboxState(PointHolder pointHolder) {
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        entity.setInstance(pointHolder.instance(), pointHolder.position().add(0.5, 1.5, 0.5));

        entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
            textDisplayMeta.setText(TextUtils.combine(
                    TextUtils.miniText("<white>Jukebox"),
                    Component.newline(),
                    TextUtils.miniText("<gray>No music playing")
            ));
            textDisplayMeta.setScale(new Vec(0.4, 0.4, 0.4));

            textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
            textDisplayMeta.setHasNoGravity(true);
        });

        displayAcquirable = entity.acquirable();
    }

    public PlayingSong currentSong() {
        return currentSong;
    }

    public boolean isPlaying() {
        if (currentSong == null) return false;
        return System.currentTimeMillis() - currentSong.startTimeMs() < currentSong.song().durationMillis();
    }

    public boolean hasQueuedSongs() {
        return !songQueue.isEmpty();
    }

    private void pushQueue(Song song) {
        songQueue.add(song);
    }

    private Song popQueue() {
        return songQueue.poll();
    }

    public void play(@NotNull PointHolder pointHolder, @NotNull Song song) {
        if (isPlaying()) {
            pushQueue(song);
            return;
        }

        currentSong = PlayingSong.of(song);
        pointHolder.instance().playSound(
                currentSong.sound(),
                pointHolder.position().add(0.5, 0.5, 0.5)
        );
    }

    public void stop(@NotNull PointHolder pointHolder) {
        if (!isPlaying()) {
            songQueue.clear();
            return;
        }

        pointHolder.instance().stopSound(currentSong.sound());

        songQueue.clear();
        currentSong = null;
    }

    public void tick(PointHolder pointHolder) {
        tickDisplay();

        if (currentSong == null) return;
        tickParticles(pointHolder);

        Song song = currentSong.song();
        long soundDuration = song.durationMillis();
        if (soundDuration <= 0) return;

        long elapsedTime = System.currentTimeMillis() - currentSong.startTimeMs();
        if (elapsedTime < soundDuration) return;

        pointHolder.instance().stopSound(currentSong.sound());
        currentSong = null;

        if (hasQueuedSongs()) play(pointHolder, popQueue());
    }

    private void tickDisplay() {
        long currentTimeMs = System.currentTimeMillis();
        if (currentTimeMs - lastDisplayTimeMs < 100) return;
        lastDisplayTimeMs = currentTimeMs;

        Component text = TextUtils.combine(
                TextUtils.miniText("<white>Jukebox"),
                Component.newline()
        );

        if (isPlaying()) {
            Song song = currentSong.song();
            long elapsedTime = System.currentTimeMillis() - currentSong.startTimeMs();

            String elapsedFormatted = TextUtils.getFormattedTime(elapsedTime, false, true);
            String durationFormatted = TextUtils.getFormattedTime(song.durationMillis(), false, true);

            text = TextUtils.combine(
                    text,
                    TextUtils.miniText("<gray>Playing: <yellow>%s", song.title()),
                    Component.newline(),
                    TextUtils.miniText("<gray>%s / %s", elapsedFormatted, durationFormatted)
            );
        } else text = TextUtils.combine(
                text,
                TextUtils.miniText("<gray>No music playing")
        );

        Component finalText = text;
        displayAcquirable.sync(entity -> {
            entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> textDisplayMeta.setText(finalText));
        });
    }

    private void tickParticles(PointHolder pointHolder) {
        long currentTimeMs = System.currentTimeMillis();
        if (currentTimeMs - lastParticleTickMs < 700) return;
        lastParticleTickMs = currentTimeMs;

        ParticleUtils.spawnParticleToViewers(
                pointHolder.instance(),
                Particle.NOTE,
                pointHolder.position().add(0.5, 1.4, 0.5),
                new Vec(0.2, 0.3, 0.2),
                0.1f,
                3
        );
    }
}
