package nl.devpieter.features.jukebox;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.features.jukebox.enums.MusicPackJobState;
import nl.devpieter.features.jukebox.events.MusicJobFailedEvent;
import nl.devpieter.features.jukebox.events.MusicJobFinishedEvent;
import nl.devpieter.features.jukebox.events.MusicJobStatusUpdateEvent;
import nl.devpieter.features.jukebox.models.FinishedJobData;
import nl.devpieter.features.jukebox.models.JobStatusResponse;
import nl.devpieter.features.jukebox.models.QueueSongsRequest;
import nl.devpieter.features.jukebox.models.QueueSongsResponse;
import nl.devpieter.sees.Sees;
import nl.devpieter.sees.annotations.SEventListener;
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

//    private final List<String> preQueuedSongs = new ArrayList<>();

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

                    if (response.getJobState() == MusicPackJobState.Completed) {
                        sees.dispatch(new MusicJobFinishedEvent(jobId, new FinishedJobData(
                                response.downloadUrl(),
                                response.processedSongs()
                        )));
                    } else if (response.getJobState() == MusicPackJobState.Failed) {
                        sees.dispatch(new MusicJobFailedEvent(jobId));
                    }

                    sees.dispatch(new MusicJobStatusUpdateEvent(jobId, response.getJobState()));
                    return null;
                });
            }
        }, TaskSchedule.nextTick(), TaskSchedule.seconds(3));
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

    @SEventListener
    public void onMusicJobFinished(MusicJobFinishedEvent event) {
        UUID jobId = event.jobId();
        finishedJobs.put(jobId, event.finishedJobData());
        activeJobs.remove(jobId);
    }

    @SEventListener
    public void onMusicJobFailed(MusicJobFailedEvent event) {
        activeJobs.remove(event.jobId());
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
