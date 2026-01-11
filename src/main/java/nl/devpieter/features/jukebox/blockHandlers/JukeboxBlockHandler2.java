package nl.devpieter.features.jukebox.blockHandlers;

import com.google.gson.Gson;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.features.jukebox.enums.MusicPackJobState;
import nl.devpieter.features.jukebox.models.JobStatusResponse;
import nl.devpieter.features.jukebox.models.ProcessedSong;
import nl.devpieter.features.jukebox.models.QueueSongsRequest;
import nl.devpieter.features.jukebox.models.QueueSongsResponse;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.features.resourcePack.models.ResourcePack;
import nl.devpieter.sees.listener.SListener;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class JukeboxBlockHandler2 implements BlockHandler, SListener {

    private final PackManager packManager = PackManager.getInstance();
    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "jukebox");
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getHand() != PlayerHand.MAIN) return false;
        Player player = interaction.getPlayer();

        List<String> songUrls = List.of(
//                "XM7BVj_C7PI" // Klaag zang
//                "fnQrPvm0_38" // Beseffen
//                "PtApgLa94Yo"
                "dbIpxy59B-U"
        );

        player.sendMessage(TextUtils.miniText("<#6a75af>Queuing your custom music pack..."));

        queueSongs(player, songUrls).thenAccept(job -> {
            System.out.println("Queued music pack job with ID: " + job.id());

            AtomicReference<Task> taskRef = new AtomicReference<>();

            Task task = scheduler.scheduleTask(() -> {
                getJobStatus(job.id()).thenAccept(jobStatusResponse -> {
                    System.out.println("Job status: " + jobStatusResponse.state());

                    player.sendActionBar(TextUtils.miniText("<#6a75af>Music Pack Job Status: " + switch (jobStatusResponse.state()) {
                        case 0 -> "Pending";
                        case 1 -> "Queued";
                        case 2 -> "Started";
                        case 3 -> "Completed";
                        case 4 -> "Failed";
                        default -> "Unknown";
                    }));

                    if (jobStatusResponse.state() == MusicPackJobState.Completed.getValue()) {
                        System.out.println("Music pack job completed!");

                        player.sendMessage(TextUtils.miniText("<#6a75af>Your custom music pack is ready! Downloading now..."));

                        UUID finishedPackId = jobStatusResponse.finishedPackId();
                        String downloadUrl = jobStatusResponse.downloadUrl();

                        if (finishedPackId != null && downloadUrl != null) {
//                            packManager.registerResourcePack(
//                                    new ResourcePack(
//                                            finishedPackId,
//                                            downloadUrl,
//                                            "",
//                                            TextUtils.miniText("<#6a75af><b>Custom Jukebox Pack</b>"),
//                                            TextUtils.miniText("<#2b2f40>Enjoy the custom music experience!"),
//                                            false,
//                                            false
//                                    )
//                            );
//
//                            packManager.tryInformRecommendation(player, finishedPackId, false);

                            if (jobStatusResponse.processedSongs().isEmpty()) {
                                System.out.println("No processed songs found in the job response.");
                                taskRef.get().cancel();
                                return;
                            }

                            ResourcePack pack = new ResourcePack(
                                    finishedPackId,
                                    downloadUrl,
                                    "",
                                    TextUtils.miniText("<#6a75af><b>Custom Jukebox Pack</b>"),
                                    TextUtils.miniText("<#2b2f40>Enjoy the custom music experience!"),
                                    false,
                                    false
                            );

                            ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                                    .prompt(pack.promptMessage())
                                    .replace(true).required(false)
                                    .packs(pack.toPackInfo())
                                    .build();

                            player.sendResourcePacks(request.callback((uuid, status, audience) -> {

                                if (status == ResourcePackStatus.DECLINED) {
                                    System.out.println("Player declined the resource pack.");
                                    return;
                                }

                                if (status == ResourcePackStatus.FAILED_DOWNLOAD) {
                                    System.out.println("Resource pack download failed.");
                                    return;
                                }

                                if (status != ResourcePackStatus.SUCCESSFULLY_LOADED) return;

                                ProcessedSong firstSong = jobStatusResponse.processedSongs().getFirst();
                                if (firstSong == null || firstSong.fileName() == null) {
                                    System.out.println("No valid processed songs found.");
                                    return;
                                }

                                player.sendMessage(TextUtils.miniText("<#6a75af>Your custom music pack has been loaded! Enjoy the tunes."));

                                System.out.println("Resource Pack Status for " + player.getUsername() + ": " + status.name() + " (Pack ID: " + finishedPackId + ")");

                                scheduler.scheduleTask(() -> {
                                    System.out.println("Playing song: " + firstSong.title() + " (" + firstSong.fileName() + ")");
                                    System.out.println("lobby:music." + firstSong.fileName());

                                    // lobby:music.9d9c4136-fd5f-4ef0-af9e-cdec6a1cde5d
                                    Key musicKey = Key.key("lobby", "music." + firstSong.fileName());
                                    Sound musicSound = Sound.sound(musicKey, Sound.Source.RECORD, 0.6f, 1.0f);

                                    player.playSound(musicSound);
                                    player.sendMessage(TextUtils.miniText("<#6a75af><b>Now playing:</b> " + firstSong.title()));

                                }, TaskSchedule.seconds(3), TaskSchedule.stop());

                            }));
                        }

                        taskRef.get().cancel();
                    } else if (jobStatusResponse.state() == MusicPackJobState.Failed.getValue()) {
                        System.out.println("Music pack job failed: " + jobStatusResponse.errorMessage());
                        taskRef.get().cancel();
                    }
                });

            }, TaskSchedule.nextTick(), TaskSchedule.seconds(5));

            taskRef.set(task);
        });

        return true;
    }

    private final Gson gson = new Gson();

    private CompletableFuture<QueueSongsResponse> queueSongs(Player player, List<String> songUrls) {
        return new AsyncRequest<QueueSongsResponse>() {

            @Override
            protected @Nullable QueueSongsResponse requestAsync() throws Exception {
                String json = gson.toJson(new QueueSongsRequest(songUrls));
                HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

                HttpResponse<String> response = simplePost(URI.create("http://localhost:5173/music-pack-jobs"), body);
                if (response.statusCode() != 202) {
                    throw new Exception("API returned " + response.statusCode() + ", expected 202");
                }

                return gson.fromJson(response.body(), QueueSongsResponse.class);
            }
        }.execute().getFuture();
    }

    private CompletableFuture<JobStatusResponse> getJobStatus(UUID jobId) {
        return new AsyncRequest<JobStatusResponse>() {

            @Override
            protected @Nullable JobStatusResponse requestAsync() throws Exception {
                HttpResponse<String> response = simpleGet(URI.create("http://localhost:5173/music-pack-jobs/" + jobId));
                if (response.statusCode() != 200) {
                    throw new Exception("API returned " + response.statusCode() + ", expected 200");
                }

                return gson.fromJson(response.body(), JobStatusResponse.class);
            }
        }.execute().getFuture();
    }
}
