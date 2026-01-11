package nl.devpieter.features.jukebox;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import nl.devpieter.features.jukebox.events.MusicJobFailedEvent;
import nl.devpieter.features.jukebox.events.MusicJobFinishedEvent;
import nl.devpieter.features.jukebox.events.MusicJobStatusUpdateEvent;
import nl.devpieter.features.jukebox.models.JukeboxSubscribeResult;
import nl.devpieter.sees.annotations.SEventListener;
import nl.devpieter.sees.listener.SListener;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MusicManager implements SListener {

    private static MusicManager INSTANCE;

    private final MusicJobManager jobManager = MusicJobManager.getInstance();

    record JukeboxJobHolder(
            UUID jobId,
            List<String> songUrls
    ) {
    }

    record JukeboxPreQueueHolder(
            List<String> songUrls
    ) {
    }

    // Player, Jukebox
    private final HashMap<UUID, UUID> subscribers = new HashMap<>();
    // JukeboxId, PlayerId
    private final HashMap<UUID, List<UUID>> jukeboxSubscribers = new HashMap<>();

    // Jukebox, JobHolder
    private final HashMap<UUID, JukeboxJobHolder> jobs = new HashMap<>();
    // JobId, JukeboxId
    private final HashMap<UUID, UUID> jobToJukebox = new HashMap<>();

    // Jukebox, PreQueueHolder
    private final HashMap<UUID, JukeboxPreQueueHolder> preQueues = new HashMap<>();

    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MusicManager();
        return INSTANCE;
    }

    public boolean isPlayerSubscribed(UUID playerId) {
        return subscribers.containsKey(playerId);
    }

    public @Nullable UUID getSubscribedJukebox(UUID playerId) {
        return subscribers.get(playerId);
    }

    public JukeboxSubscribeResult subscribe(UUID playerId, UUID jukeboxId) {
        UUID previousJukeboxId = subscribers.put(playerId, jukeboxId);

        if (previousJukeboxId != null) {
            List<UUID> previousSubscribers = jukeboxSubscribers.get(previousJukeboxId);
            if (previousSubscribers != null) previousSubscribers.remove(playerId);
        }

        List<UUID> currentSubscribers = jukeboxSubscribers.getOrDefault(jukeboxId, new ArrayList<>());
        if (!currentSubscribers.contains(playerId)) {
            currentSubscribers.add(playerId);
            jukeboxSubscribers.put(jukeboxId, currentSubscribers);
        }

        return new JukeboxSubscribeResult(playerId, previousJukeboxId, jukeboxId);
    }

    public boolean unsubscribe(UUID playerId, UUID jukeboxId) {
        UUID subscribedJukeboxId = subscribers.get(playerId);
        if (subscribedJukeboxId == null || !subscribedJukeboxId.equals(jukeboxId)) return false;

        subscribers.remove(playerId);

        List<UUID> currentSubscribers = jukeboxSubscribers.get(jukeboxId);
        if (currentSubscribers != null) currentSubscribers.remove(playerId);

        return true;
    }

    public boolean playerPreQueueSong(UUID playerId, String songUrl) {
        UUID jukeboxId = subscribers.get(playerId);
        if (jukeboxId == null) return false;

        return preQueueSong(jukeboxId, songUrl);
    }

    public boolean preQueueSong(UUID jukeboxId, String songUrl) {
        JukeboxPreQueueHolder preQueueHolder = preQueues.get(jukeboxId);

        if (preQueueHolder == null) {
            preQueueHolder = new JukeboxPreQueueHolder(new ArrayList<>());
            preQueues.put(jukeboxId, preQueueHolder);
        }

        preQueueHolder.songUrls().add(songUrl);
        return true;
    }

    public CompletableFuture<Boolean> queueSongs(UUID jukeboxId) {
        JukeboxPreQueueHolder preQueueHolder = preQueues.remove(jukeboxId);

        if (preQueueHolder == null || preQueueHolder.songUrls().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return jobManager.queueJobAsync(preQueueHolder.songUrls()).thenApply(jobId -> {
            if (jobId == null) return false;

            JukeboxJobHolder jobHolder = new JukeboxJobHolder(
                    jobId, new ArrayList<>(preQueueHolder.songUrls())
            );

            jobs.put(jukeboxId, jobHolder);
            jobToJukebox.put(jobId, jukeboxId);
            return true;
        });
    }

    @SEventListener
    public void onMusicJobFailed(MusicJobFailedEvent event) {
        removeJob(event.jobId());
    }

    @SEventListener
    public void onMusicJobFinished(MusicJobFinishedEvent event) {
        removeJob(event.jobId());
    }

    private void removeJob(UUID jobId) {
        UUID jukeboxId = jobToJukebox.get(jobId);
        if (jukeboxId == null) return;

        jobs.remove(jukeboxId);
        jobToJukebox.remove(jobId);
    }

    @SEventListener
    public void onMusicJobStatusUpdate(MusicJobStatusUpdateEvent event) {
        UUID jobId = event.jobId();

        UUID jukeboxId = jobToJukebox.get(jobId);
        if (jukeboxId == null) return;

        List<UUID> subscribedPlayers = jukeboxSubscribers.get(jukeboxId);
        if (subscribedPlayers == null) return;

        for (UUID playerId : subscribedPlayers) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId);
            if (player == null || !player.isOnline()) continue;

            player.sendActionBar(TextUtils.combine(
                    TextUtils.miniText("<gray>Jukebox Status Update:"),
                    Component.space(),
                    TextUtils.miniText("<b>" + event.status().name())
            ));
        }
    }

    @SEventListener
    public void onMusicJobFinished2(MusicJobFinishedEvent event) {
        UUID jobId = event.jobId();

        UUID jukeboxId = jobToJukebox.get(jobId);
        if (jukeboxId == null) return;

        List<UUID> subscribedPlayers = jukeboxSubscribers.get(jukeboxId);
        if (subscribedPlayers == null) return;

        for (UUID playerId : subscribedPlayers) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId);
            if (player == null || !player.isOnline()) continue;

            player.sendMessage(TextUtils.combine(
                    TextUtils.miniText("<green>Jukebox Job Finished!")
            ));
        }
    }
}
