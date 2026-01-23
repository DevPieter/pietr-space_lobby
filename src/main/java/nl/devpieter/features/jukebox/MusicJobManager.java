package nl.devpieter.features.jukebox;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.features.jukebox.enums.MusicPackJobState;
import nl.devpieter.features.jukebox.events.MusicPackJobFailedEvent;
import nl.devpieter.features.jukebox.events.MusicPackJobFinishedEvent;
import nl.devpieter.features.jukebox.events.MusicPackJobStatusUpdateEvent;
import nl.devpieter.features.jukebox.models.FinishedJobData;
import nl.devpieter.features.jukebox.models.JobStatusResponse;
import nl.devpieter.features.jukebox.models.QueueSongsRequest;
import nl.devpieter.features.jukebox.models.QueueSongsResponse;
import nl.devpieter.sees.Sees;
import nl.devpieter.sees.listener.SListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MusicJobManager implements SListener {

    private static MusicJobManager INSTANCE;

    private final Gson gson = new Gson();
    private final Sees sees = Sees.getInstance();
    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private final List<UUID> activeJobs = new ArrayList<>();
    private final HashMap<UUID, FinishedJobData> finishedJobs = new HashMap<>();

    private MusicJobManager() {
    }

    public static MusicJobManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MusicJobManager();
        return INSTANCE;
    }

    public void init() {
        // We use events so that we can easily switch to sockets or other communication methods in the future.
        scheduler.scheduleTask(() -> {
            List<UUID> jobsToCheck = new ArrayList<>(activeJobs);

            for (UUID jobId : jobsToCheck) {
                sendStatusRequestAsync(jobId).thenApply(response -> {
                    if (response == null) {
                        System.out.println("Failed to get status for job " + jobId);
                        return null;
                    }

                    System.out.println("Job " + jobId + " is in state " + response.getJobState());

                    // This should never happen, but just in case.
                    if (finishedJobs.containsKey(jobId)) {
                        activeJobs.remove(jobId);
                        return null;
                    }

                    if (response.getJobState() == MusicPackJobState.Completed) {

                        if (response.finishedPackId() == null
                                || response.downloadUrl() == null
                                || response.processedSongs() == null
                        ) {
                            System.out.println("Job " + jobId + " is marked as completed but is missing data.");

                            activeJobs.remove(jobId);
                            sees.dispatch(new MusicPackJobFailedEvent(jobId));
                            return null;
                        }

                        FinishedJobData jobData = new FinishedJobData(
                                response.finishedPackId(),
                                response.downloadUrl(),
                                response.processedSongs()
                        );

                        finishedJobs.put(jobId, jobData);
                        activeJobs.remove(jobId);

                        sees.dispatch(new MusicPackJobFinishedEvent(jobId, jobData));
                    } else if (response.getJobState() == MusicPackJobState.Failed) {
                        activeJobs.remove(jobId);
                        sees.dispatch(new MusicPackJobFailedEvent(jobId));
                    }

                    sees.dispatch(new MusicPackJobStatusUpdateEvent(jobId, response.getJobState()));
                    return null;
                });
            }
        }, TaskSchedule.nextTick(), TaskSchedule.seconds(3));
    }

    public boolean isJobActive(@NotNull UUID jobId) {
        return activeJobs.contains(jobId);
    }

    public boolean isJobFinished(@NotNull UUID jobId) {
        return finishedJobs.containsKey(jobId);
    }

    public @Nullable FinishedJobData getJobData(@NotNull UUID jobId) {
        return finishedJobs.get(jobId);
    }

    public CompletableFuture<@Nullable UUID> queueJobAsync(List<String> songsToQueue) {
        if (songsToQueue.isEmpty()) return CompletableFuture.completedFuture(null);
        QueueSongsRequest request = new QueueSongsRequest(songsToQueue);

        return sendQueueRequestAsync(request).thenApply(response -> {
            if (response == null) return null;

            activeJobs.add(response.id());
            return response.id();
        });
    }

    private @NotNull CompletableFuture<@Nullable QueueSongsResponse> sendQueueRequestAsync(QueueSongsRequest request) {
        return new AsyncRequest<@Nullable QueueSongsResponse>() {

            @Override
            protected @Nullable QueueSongsResponse requestAsync() throws Exception {
                String json = gson.toJson(request);
                HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

                HttpResponse<String> response = simplePost(URI.create("http://localhost:5173/music-pack-jobs"), body);
                if (response.statusCode() != 202) {
                    throw new Exception("API returned " + response.statusCode() + ", expected 202");
                }

                return gson.fromJson(response.body(), QueueSongsResponse.class);
            }
        }.execute().getFuture();
    }

    private @NotNull CompletableFuture<@Nullable JobStatusResponse> sendStatusRequestAsync(UUID jobId) {
        return new AsyncRequest<@Nullable JobStatusResponse>() {

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
