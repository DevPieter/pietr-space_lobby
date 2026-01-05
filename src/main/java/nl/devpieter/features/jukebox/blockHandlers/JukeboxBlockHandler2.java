package nl.devpieter.features.jukebox.blockHandlers;

import com.google.gson.Gson;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.features.resourcePack.models.ResourcePack;
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

public class JukeboxBlockHandler2 implements BlockHandler {

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
                "XM7BVj_C7PI"
        );

        queueSongs(player, songUrls).thenAccept(jobId -> {
            System.out.println("Queued music pack job with ID: " + jobId);

            AtomicReference<Task> taskRef = new AtomicReference<>();

            Task task = scheduler.scheduleTask(() -> {
                getJobStatus(jobId).thenAccept(jobStatusResponse -> {
                    System.out.println("Job status: " + jobStatusResponse.state);

                    if (jobStatusResponse.state == MusicPackJobState.Completed.getValue()) {
                        System.out.println("Music pack job completed!");
                        UUID finishedPackId = jobStatusResponse.finishedPackId();
                        String downloadUrl = jobStatusResponse.downloadUrl();

                        if (finishedPackId != null && downloadUrl != null) {
                            packManager.registerResourcePack(
                                    new ResourcePack(
                                            finishedPackId,
                                            downloadUrl,
                                            "",
                                            TextUtils.miniText("<#6a75af><b>Custom Jukebox Pack</b>"),
                                            TextUtils.miniText("<#2b2f40>Enjoy the custom music experience!"),
                                            false,
                                            false
                                    )
                            );

                            packManager.tryInformRecommendation(player, finishedPackId, false);
                        }

                        taskRef.get().cancel();
                    } else if (jobStatusResponse.state == MusicPackJobState.Failed.getValue()) {
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

    record QueueSongRequest(List<String> trackUrls) {
    }

    record QueueSongResponse(String id) {
    }

    record JobStatusResponse(
            int state,
            @Nullable UUID finishedPackId,
            @Nullable String downloadUrl,
            @Nullable String errorMessage
    ) {
    }

    enum MusicPackJobState {
        Pending(0),
        Queued(1),
        Started(2),
        Completed(3),
        Failed(4);

        private final int value;

        MusicPackJobState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private CompletableFuture<UUID> queueSongs(Player player, List<String> songUrls) {
        return new AsyncRequest<UUID>() {

            // POST http://localhost:5173/music-pack-jobs
            // Body: { "trackUrls": ["url", "url"] }

            // Returns 202 Accepted
            // Response body: { "id": "019b8f80-785b-77f0-a8a1-a571e228f92a" }

            @Override
            protected @Nullable UUID requestAsync() throws Exception {
                String json = gson.toJson(new QueueSongRequest(songUrls));
                HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

                HttpResponse<String> response = simplePost(URI.create("http://localhost:5173/music-pack-jobs"), body);
                if (response.statusCode() != 202) {
                    throw new Exception("API returned " + response.statusCode() + ", expected 202");
                }

                QueueSongResponse queueSongResponse = gson.fromJson(response.body(), QueueSongResponse.class);
                return UUID.fromString(queueSongResponse.id);
            }
        }.execute().getFuture();
    }

    private CompletableFuture<JobStatusResponse> getJobStatus(UUID jobId) {
        return new AsyncRequest<JobStatusResponse>() {

            // GET http://localhost:5173/music-pack-jobs/{jobId}

            // Returns 200 OK
            // Response body: { "state": 0, "finishedPackId": "019b8f80-785b-77f0-a8a1-a571e228f92a", "errorMessage": null }

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
