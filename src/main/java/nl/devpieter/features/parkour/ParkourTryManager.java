package nl.devpieter.features.parkour;

import com.google.gson.Gson;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.config.ConfigManager;
import nl.devpieter.features.parkour.models.ParkourLeaderboardEntry;
import nl.devpieter.features.parkour.models.parkourTry.ParkourTry;
import nl.devpieter.features.parkour.requests.CreateParkourTryRequest;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ParkourTryManager {

    private static ParkourTryManager INSTANCE;

    private final ComponentLogger logger = MinecraftServer.LOGGER;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private final Gson gson = new Gson();
    private final URI uri = URI.create(configManager.getString("api_base_url") + "/api/mc/lobby/parkour/try");

    private final List<ParkourTry> queuedParkourTries = new ArrayList<>();

    //    private final HashMap<UUID, ParkourLeaderboardEntry> playerLeaderboardLookup = new HashMap<>();
    private final HashMap<String, ParkourLeaderboardEntry> parkourLeaderboardLookup = new HashMap<>();

    private Task queueTask;

    private ParkourTryManager() {
    }

    public static ParkourTryManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ParkourTryManager();
        return INSTANCE;
    }

    public void init() {
        if (queueTask != null) queueTask.cancel();
        queueTask = scheduler.scheduleTask(
                this::handleQueuedParkourTries,
                TaskSchedule.nextTick(),
                TaskSchedule.seconds(30)
        );
    }

    public void stop(boolean save) {
        if (save) handleQueuedParkourTries();
        if (queueTask == null) return;

        queueTask.cancel();
        queueTask = null;
    }

    public void handleParkourTry(Player player, ParkourTry parkourTry) {
        queuedParkourTries.add(parkourTry);
        if (parkourTry.hasFinished()) handleFinish(player, parkourTry);
    }

    private void handleFinish(Player player, ParkourTry parkourTry) {
        long completionTime = parkourTry.completionTime();
        String time = TextUtils.getFormattedTime(completionTime, true, false);

        if (updatePersonalBest(parkourTry)) {
            player.sendMessage(TextUtils.miniText("<green>You've achieved a new personal best of <yellow><b>%s</b></yellow>!", time));
            player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1.0f, 1.2f));
        } else {
            player.sendMessage(TextUtils.miniText("<green>You finished the parkour in <yellow><b>%s</b></yellow>!", time));
            player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

    private boolean updatePersonalBest(ParkourTry parkourTry) {
        ParkourLeaderboardEntry existingEntry = parkourLeaderboardLookup.get(parkourTry.parkourId());
        if (existingEntry != null && parkourTry.completionTime() >= existingEntry.completionTime()) return false;

        parkourLeaderboardLookup.put(parkourTry.parkourId(), new ParkourLeaderboardEntry(
                parkourTry.parkourId(),
                parkourTry.playerUuid(),
                parkourTry.completionTime()
        ));

        return true;
    }

    private void handleQueuedParkourTries() {
        if (queuedParkourTries.isEmpty()) return;

        List<ParkourTry> parkourTries = new ArrayList<>(queuedParkourTries);
        queuedParkourTries.clear();

        for (ParkourTry parkourTry : parkourTries) {
            CreateParkourTryRequest parkourTryRequest = CreateParkourTryRequest.of(parkourTry);

            postParkourTry(parkourTryRequest).exceptionally(e -> {
                logger.error("Failed to post ParkourTry for player: {} - {}", parkourTry.playerUuid(), e.getMessage());
                return null;
            });
        }
    }

    private CompletableFuture<Void> postParkourTry(CreateParkourTryRequest parkourTry) {
        return new AsyncRequest<Void>() {
            @Override
            protected @Nullable Void requestAsync() throws Exception {

                String json = gson.toJson(parkourTry);
                HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

                HttpResponse<String> response = simplePost(uri, body);
                if (response.statusCode() != 201) {
                    logger.error("API returned {}, expected 201", response.statusCode());
                    throw new Exception("API returned " + response.statusCode() + ", expected 201");
                }

                return null;
            }
        }.execute().getFuture();
    }
}
