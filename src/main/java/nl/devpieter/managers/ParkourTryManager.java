package nl.devpieter.managers;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.Main;
import nl.devpieter.Settings;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.models.parkour.ParkourTry;
import nl.devpieter.models.requests.CreateParkourTryRequest;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ParkourTryManager {

    private static ParkourTryManager INSTANCE;

    private final URI uri = URI.create(Settings.API_BASE_URL + "/api/mc/lobby/parkour/try");
    private final Gson gson = new Gson();

    private final Logger logger = MinecraftServer.LOGGER;
    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private Task task;

    private final List<ParkourTry> queuedParkourTries = new ArrayList<>();

    private ParkourTryManager() {
    }

    public static ParkourTryManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourTryManager();
        return INSTANCE;
    }

    public void start() {
        if (task != null) task.cancel();
        task = scheduler.scheduleTask(() -> {
            if (queuedParkourTries.isEmpty()) return;

            boolean debug = Main.DEBUG;
            if (debug) logger.info("Processing queued ParkourTries, count: {}", queuedParkourTries.size());

            List<ParkourTry> parkourTries = new ArrayList<>(queuedParkourTries);
            queuedParkourTries.clear();

            for (ParkourTry parkourTry : parkourTries) {
                CreateParkourTryRequest parkourTryRequest = CreateParkourTryRequest.of(parkourTry);
                if (debug) logger.info("Posting ParkourTry for player: {}", parkourTry.playerUuid());

                postParkourTry(parkourTryRequest).exceptionally(e -> {
                    logger.error("Failed to post ParkourTry for player: {} - {}", parkourTry.playerUuid(), e.getMessage());
                    return null;
                });
            }

            if (debug) logger.info("Done processing ParkourTries");
        }, TaskSchedule.seconds(5), TaskSchedule.seconds(15));
    }

    public void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }

    public void queueParkourTry(ParkourTry parkourTry) {
        queuedParkourTries.add(parkourTry);
        if (Main.DEBUG) logger.info("Queued ParkourTry for player: {}", parkourTry.playerUuid());
    }

    private CompletableFuture<Void> postParkourTry(CreateParkourTryRequest parkourTry) {

        return new AsyncRequest<Void>() {
            @Override
            protected @Nullable Void requestAsync() throws Exception {

                String json = gson.toJson(parkourTry);
                HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

                HttpResponse<String> response = simplePost(uri, body);
                if (response.statusCode() != 201) {
                    throw new Exception("API returned " + response.statusCode() + ", expected 201");
                }

                if (Main.DEBUG) {
                    logger.info("Successfully posted ParkourTry for player: {}", parkourTry.playerUuid());
                }

                return null;
            }
        }.execute().getFuture();
    }
}
