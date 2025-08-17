package nl.devpieter.managers;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.models.parkour.ParkourTry;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ParkourTryManager {

    private static ParkourTryManager INSTANCE;

    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private Task task;

    private final List<ParkourTry> queuedParkourTries = new ArrayList<>();

    private ParkourTryManager() {
        // Runnable task, TaskSchedule delay, TaskSchedule repeat

    }

    public static ParkourTryManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourTryManager();
        return INSTANCE;
    }

    public void start() {
        if (task != null) task.cancel();
        task = scheduler.scheduleTask(() -> {
            if (queuedParkourTries.isEmpty()) return;

            List<ParkourTry> parkourTries = new ArrayList<>(queuedParkourTries);
            queuedParkourTries.clear();

            for (ParkourTry parkourTry : parkourTries) {
                processParkourTry(parkourTry).thenAccept(v -> {
                    System.out.println("Processed ParkourTry for player: " + parkourTry.playerUuid());
                }).exceptionally(e -> {
                    // Handle exception
                    e.printStackTrace();
                    return null;
                });
            }
        }, TaskSchedule.seconds(5), TaskSchedule.seconds(10));
    }

    public void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }

    public void queueParkourTry(ParkourTry parkourTry) {
        queuedParkourTries.add(parkourTry);
    }

    private final URI uri = URI.create("http://localhost:5193/api/mc/lobby/parkour/try");
    private final Gson gson = new Gson();

    private CompletableFuture<Void> processParkourTry(ParkourTry parkourTry) {

        return new AsyncRequest<Void>() {
            @Override
            protected @Nullable Void requestAsync() throws Exception {

                String json = gson.toJson(parkourTry);
                HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

                HttpResponse<String> response = simplePost(uri, body);
                if (response.statusCode() != 201) {
                    throw new Exception("API returned " + response.statusCode() + ", expected 201");
                }

                return null;
            }
        }.execute().getFuture();
    }
}
