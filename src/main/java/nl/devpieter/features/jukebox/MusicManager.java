package nl.devpieter.features.jukebox;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.features.jukebox.events.MusicPackJobFailedEvent;
import nl.devpieter.features.jukebox.events.MusicPackJobFinishedEvent;
import nl.devpieter.features.jukebox.events.MusicPackJobStatusUpdateEvent;
import nl.devpieter.features.jukebox.models.FinishedJobData;
import nl.devpieter.features.jukebox.models.JukeboxSubscribeResult;
import nl.devpieter.features.jukebox.models.ProcessedSong;
import nl.devpieter.sees.Sees;
import nl.devpieter.sees.annotations.SEventListener;
import nl.devpieter.sees.event.SEvent;
import nl.devpieter.sees.listener.SListener;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MusicManager implements SListener {

    private static MusicManager INSTANCE;

    private final MusicJobManager jobManager = MusicJobManager.getInstance();

    private record JobHolder(
            List<String> songUrls
    ) {
    }

    private static class JobsHolder {

        private final HashMap<UUID, JobHolder> jobs = new HashMap<>();

        public @NotNull List<JobHolder> jobs() {
            return new ArrayList<>(jobs.values());
        }

        public void add(UUID jobId, JobHolder jobHolder) {
            jobs.put(jobId, jobHolder);
        }

        public void removeByJobId(UUID jobId) {
            jobs.remove(jobId);
        }

        public JobHolder getByJobId(UUID jobId) {
            return jobs.get(jobId);
        }
    }

    private static class PreQueueHolder {

        private final List<String> songUrls = new ArrayList<>();

        public @NotNull List<String> songUrls() {
            return new ArrayList<>(songUrls);
        }

        public void add(String songUrl) {
            songUrls.add(songUrl);
        }
    }

    private record FinishedSongHolder(
            UUID jobId,
            UUID processedSongId
    ) {
    }

    private static class FinishedSongsHolder {

        // OriginalUrl, FinishedSongHolder
        private final HashMap<String, FinishedSongHolder> finishedSongs = new HashMap<>();

        public @NotNull List<FinishedSongHolder> finishedSongs() {
            return new ArrayList<>(finishedSongs.values());
        }

        public void add(String originalUrl, FinishedSongHolder finishedSongHolder) {
            finishedSongs.put(originalUrl, finishedSongHolder);
        }

        public void addAll(HashMap<String, FinishedSongHolder> songs) {
            finishedSongs.putAll(songs);
        }

        public FinishedSongHolder getByOriginalUrl(String originalUrl) {
            return finishedSongs.get(originalUrl);
        }
    }

    // PlayerId, JukeboxId // JukeboxId, PlayerIds
    private final HashMap<UUID, UUID> subscribedPlayers = new HashMap<>();
    private final HashMap<UUID, List<UUID>> jukeboxToSubscribers = new HashMap<>();

    // JukeboxId, JobHolders // JukeboxId, JobHolders // JobId, JukeboxId
    private final HashMap<UUID, JobsHolder> activeJukeboxJobs = new HashMap<>();
    private final HashMap<UUID, JobsHolder> finishedJukeboxJobs = new HashMap<>();
    private final HashMap<UUID, UUID> jobToJukebox = new HashMap<>();

    // JukeboxId, JukeboxPreQueueHolder
    private final HashMap<UUID, PreQueueHolder> preQueues = new HashMap<>();

    // JukeboxId, FinishedSongsHolder
    private final HashMap<UUID, FinishedSongsHolder> finishedSongs = new HashMap<>();

    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MusicManager();
        return INSTANCE;
    }

    public boolean isPlayerSubscribed(UUID playerId) {
        return subscribedPlayers.containsKey(playerId);
    }

    public @Nullable UUID getSubscribedJukebox(UUID playerId) {
        return subscribedPlayers.get(playerId);
    }

    public JukeboxSubscribeResult subscribe(UUID playerId, UUID jukeboxId) {
        UUID previousJukeboxId = subscribedPlayers.put(playerId, jukeboxId);

        if (previousJukeboxId != null) {
            List<UUID> previousSubscribers = jukeboxToSubscribers.get(previousJukeboxId);
            if (previousSubscribers != null) previousSubscribers.remove(playerId);
        }

        jukeboxToSubscribers.computeIfAbsent(jukeboxId, _ -> new ArrayList<>()).add(playerId);
        return new JukeboxSubscribeResult(playerId, previousJukeboxId, jukeboxId);
    }

    public boolean unsubscribe(UUID playerId, UUID jukeboxId) {
        UUID subscribedJukeboxId = subscribedPlayers.get(playerId);
        if (subscribedJukeboxId == null || !subscribedJukeboxId.equals(jukeboxId)) return false;

        subscribedPlayers.remove(playerId);

        List<UUID> currentSubscribers = jukeboxToSubscribers.get(jukeboxId);
        if (currentSubscribers != null) {
            currentSubscribers.remove(playerId);
            if (currentSubscribers.isEmpty()) jukeboxToSubscribers.remove(jukeboxId);
        }

        return true;
    }

    public boolean playerPreQueueSong(UUID playerId, String songUrl) {
        UUID jukeboxId = subscribedPlayers.get(playerId);
        if (jukeboxId == null) return false;

        return preQueueSong(jukeboxId, songUrl);
    }

    public boolean preQueueSong(UUID jukeboxId, String songUrl) {
        preQueues.computeIfAbsent(jukeboxId, _ -> new PreQueueHolder()).add(songUrl);
        return true;
    }

    public CompletableFuture<Boolean> queueSongs(UUID jukeboxId) {
        PreQueueHolder preQueueHolder = preQueues.remove(jukeboxId);

        if (preQueueHolder == null || preQueueHolder.songUrls().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        List<String> songsToQueue = preQueueHolder.songUrls();

        return jobManager.queueJobAsync(songsToQueue).thenApply(jobId -> {
            if (jobId == null) return false;

            activeJukeboxJobs.computeIfAbsent(jukeboxId, _ -> new JobsHolder()).add(
                    jobId,
                    new JobHolder(songsToQueue)
            );

            jobToJukebox.put(jobId, jukeboxId);
            return true;
        });
    }

    @SEventListener
    public void onMusicJobFailed(MusicPackJobFailedEvent event) {
        UUID jukeboxId = jobToJukebox.get(event.jobId());
        if (jukeboxId == null) return;

        JobsHolder activeJobsHolder = activeJukeboxJobs.get(jukeboxId);
        if (activeJobsHolder == null) return;

        activeJobsHolder.removeByJobId(event.jobId());
        if (activeJobsHolder.jobs().isEmpty()) activeJukeboxJobs.remove(jukeboxId);

        // TODO - Check if we should keep this or just delete it
//        jobToJukebox.remove(event.jobId());

        System.out.println("Music Job Failed Event processed for job " + event.jobId());
    }

    @SEventListener
    public void onMusicJobFinished(MusicPackJobFinishedEvent event) {
        UUID jukeboxId = jobToJukebox.get(event.jobId());
        if (jukeboxId == null) return;

        JobsHolder activeJobsHolder = activeJukeboxJobs.get(jukeboxId);
        if (activeJobsHolder == null) return;

        JobHolder jobHolder = activeJobsHolder.getByJobId(event.jobId());
        if (jobHolder == null) return;

        activeJobsHolder.removeByJobId(event.jobId());
        if (activeJobsHolder.jobs().isEmpty()) activeJukeboxJobs.remove(jukeboxId);

        finishedJukeboxJobs.computeIfAbsent(jukeboxId, _ -> new JobsHolder()).add(
                event.jobId(),
                jobHolder
        );

//        event.finishedJobData()

//        List<FinishedSongHolder> songs = new ArrayList<>();
//
//        for (ProcessedSong song : event.finishedJobData().songs()) {
//            songs.add(new FinishedSongHolder(event.jobId(), song.id()));
//        }
//
//        finishedSongs.computeIfAbsent(jukeboxId, _ -> new FinishedSongsHolder()).addAll(
//                jobHolder.songUrls(),
//                songs
//        );

        HashMap<String, FinishedSongHolder> songs = new HashMap<>();

        for (ProcessedSong song : event.finishedJobData().songs()) {
            songs.put(song.originalUrl(), new FinishedSongHolder(event.jobId(), song.id()));
        }

        finishedSongs.computeIfAbsent(jukeboxId, _ -> new FinishedSongsHolder()).addAll(songs);

        System.out.println("Music Job Finished Event processed for job " + event.jobId());
    }

//    private void removeJob(UUID jobId) {
//        UUID jukeboxId = jobToJukebox.get(jobId);
//        if (jukeboxId == null) return;
//
//        JobsHolder jobsHolder = activeJukeboxJobs.get(jukeboxId);
//        if (jobsHolder != null) {
//            jobsHolder.removeByJobId(jobId);
//            if (jobsHolder.jobs().isEmpty()) activeJukeboxJobs.remove(jukeboxId);
//        }
//
//        jobToJukebox.remove(jobId);
//    }

    @SEventListener
    public void onMusicJobStatusUpdate(MusicPackJobStatusUpdateEvent event) {
        UUID jobId = event.jobId();

        UUID jukeboxId = jobToJukebox.get(jobId);
        if (jukeboxId == null) return;

        Component message = TextUtils.combine(
                TextUtils.miniText("<gray>Jukebox Job Status Updated:"),
                Component.space(),
                TextUtils.miniText("<b>" + event.status().name())
        );

        notifyPlayers(jukeboxId, message, true);
    }

//    public record JukeboxPackStatus(
//            UUID jukeboxId,
//            UUID playerId,
//            UUID jobId
//    ) {
//    }

    private final HashMap<UUID, List<UUID>> waitingForPackAcceptance = new HashMap<>();
    //    private final HashMap<UUID, List<FinishedJobData
//    private final HashMap<UUID, HashMap<UUID, FinishedJobData>> jukeboxFinishedJobs = new HashMap<>();

    // JukeboxId, FinishedJobData
    private final HashMap<UUID, FinishedJobData> jukeboxFinishedJobs = new HashMap<>();

    public record PackAcceptanceUpdatedEvent(
            UUID jukeboxId,
            UUID playerId,
            ResourcePackStatus status
    ) implements SEvent {
    }

    @SEventListener
    public void onMusicJobFinished2(MusicPackJobFinishedEvent event) {
        System.out.println("Music Job Finished Event received for job " + event.jobId());
        UUID jobId = event.jobId();

        UUID jukeboxId = jobToJukebox.get(jobId);
        if (jukeboxId == null) {
            System.out.println("No jukebox found for job " + jobId);
            return;
        }

//        jukeboxFinishedJobs.computeIfAbsent(jukeboxId, _ -> new HashMap<>()).put(jobId, event.finishedJobData());
        jukeboxFinishedJobs.put(jukeboxId, event.finishedJobData());

        List<UUID> subscribedPlayers = jukeboxToSubscribers.get(jukeboxId);
        if (subscribedPlayers == null) {
            System.out.println("No subscribed players for jukebox " + jukeboxId);
            return;
        }

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .prompt(TextUtils.miniText("<#6a75af><b>Custom Jukebox Pack</b>"))
                .replace(true).required(false)
                .packs(ResourcePackInfo.resourcePackInfo()
                        .id(event.finishedJobData().packId())
                        .uri(URI.create(event.finishedJobData().downloadUrl()))
                        .hash("")
                        .build())
                .build();

        for (UUID playerId : subscribedPlayers) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId);
            if (player == null || !player.isOnline()) continue;

            player.sendMessage(TextUtils.combine(
                    TextUtils.miniText("<green>Jukebox Job Finished!")
            ));

            waitingForPackAcceptance.computeIfAbsent(jukeboxId, k -> new ArrayList<>()).add(playerId);

            player.sendResourcePacks(request.callback((uuid, status, audience) -> {
                List<UUID> waitingPlayers = waitingForPackAcceptance.get(jukeboxId);
                if (waitingPlayers != null) {
                    waitingPlayers.remove(playerId);
                    if (waitingPlayers.isEmpty()) waitingForPackAcceptance.remove(jukeboxId);
                }

                if (status != ResourcePackStatus.SUCCESSFULLY_LOADED
                        && status != ResourcePackStatus.DECLINED
                        && status != ResourcePackStatus.FAILED_DOWNLOAD
                        && status != ResourcePackStatus.FAILED_RELOAD
                ) return;

                player.sendMessage(TextUtils.combine(
                        TextUtils.miniText("<gray>Jukebox Pack Status:"),
                        Component.space(),
                        switch (status) {
                            case SUCCESSFULLY_LOADED -> TextUtils.miniText("<green>Loaded successfully!");
                            case DECLINED -> TextUtils.miniText("<yellow>Declined the pack.");
                            case FAILED_DOWNLOAD -> TextUtils.miniText("<red>Failed to download the pack.");
                            default -> TextUtils.miniText("<gray>Unknown status.");
                        }
                ));

                Sees.getInstance().dispatch(new PackAcceptanceUpdatedEvent(jukeboxId, playerId, status));
            }));
        }
    }

    @SEventListener
    public void onPackAcceptanceUpdated(PackAcceptanceUpdatedEvent event) {
        waitingForPackAcceptance.computeIfAbsent(event.jukeboxId(), k -> new ArrayList<>()).remove(event.playerId());

        List<UUID> waitingPlayers = waitingForPackAcceptance.get(event.jukeboxId());
        if (waitingPlayers == null || waitingPlayers.isEmpty()) {
            waitingForPackAcceptance.remove(event.jukeboxId());

            Component message = TextUtils.miniText("<green>All players have responded to the jukebox pack.");
            notifyPlayers(event.jukeboxId(), message, false);

            FinishedJobData finishedJobData = jukeboxFinishedJobs.remove(event.jukeboxId());
            List<ProcessedSong> songs = finishedJobData.songs();
            if (songs == null) {
                notifyPlayers(event.jukeboxId(), TextUtils.miniText("<red>No songs were processed in the jukebox pack."), false);
                return;
            }

            Component songListMessage = TextUtils.miniText("<gray>Jukebox Pack Songs:");

            for (ProcessedSong song : songs) {
                songListMessage = TextUtils.combine(
                        songListMessage,
                        Component.newline(),
                        TextUtils.miniText("<gray>- %s", song.title())
                );
            }

            notifyPlayers(event.jukeboxId(), songListMessage, false);

            ProcessedSong firstSong = songs.getFirst();
            if (firstSong == null) {
                notifyPlayers(event.jukeboxId(), TextUtils.miniText("<red>No songs available to play in the jukebox pack."), false);
                return;
            }

            Key musicKey = Key.key("lobby", "music." + firstSong.fileName());
            Sound musicSound = Sound.sound(musicKey, Sound.Source.RECORD, 0.6f, 1.0f);

            Component nowPlayingMessage = TextUtils.combine(
                    TextUtils.miniText("<gray>Now playing first song from the jukebox pack:"),
                    Component.newline(),
                    TextUtils.miniText("<yellow>%s", firstSong.title())
            );

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                iterateSubscribedPlayers(event.jukeboxId(), player -> {
                    player.sendMessage(nowPlayingMessage);
                    player.playSound(musicSound);
                });
            }, TaskSchedule.seconds(1), TaskSchedule.stop());
            return;
        }

        Component message = TextUtils.combine(
                TextUtils.miniText("<gray>Waiting for other players to accept the jukebox pack..."),
                Component.space(),
                TextUtils.miniText("<yellow>%d player(s) remaining", waitingPlayers.size())
        );

        notifyPlayers(event.jukeboxId(), message, true);
    }

    private void notifyPlayers(UUID jukeboxId, Component message, boolean actionBar) {
        iterateSubscribedPlayers(jukeboxId, player -> {
            if (actionBar) player.sendActionBar(message);
            else player.sendMessage(message);
        });
    }

    private void iterateSubscribedPlayers(UUID jukeboxId, Consumer<Player> consumer) {
        List<UUID> subscribedPlayers = jukeboxToSubscribers.get(jukeboxId);
        if (subscribedPlayers == null) return;

        for (UUID playerId : subscribedPlayers) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId);
            if (player == null || !player.isOnline()) continue;

            consumer.accept(player);
        }
    }
}
