package nl.devpieter.features.parkour;

import com.google.gson.Gson;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.api.request.AsyncRequest;
import nl.devpieter.config.ConfigManager;
import nl.devpieter.features.parkour.models.ParkourLeaderboardEntry;
import nl.devpieter.features.parkour.models.parkour.Parkour;
import nl.devpieter.features.parkour.models.parkourTry.ParkourTry;
import nl.devpieter.features.parkour.requests.CreateParkourTryRequest;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ParkourTryManager {

    private static ParkourTryManager INSTANCE;

    private final ComponentLogger logger = MinecraftServer.LOGGER;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final Scheduler scheduler = MinecraftServer.getSchedulerManager();

    private final Gson gson = new Gson();
    //    private final URI uri = URI.create(configManager.getString("api_base_url") + "/api/mc/lobby/parkour/try");
    private final URI uri = URI.create("http://localhost:5246/api/parkour/test");

    private final List<ParkourTry> queuedParkourTries = new ArrayList<>();

    //    private final HashMap<UUID, ParkourLeaderboardEntry> playerLeaderboardLookup = new HashMap<>();
//    private final HashMap<String, ParkourLeaderboardEntry> parkourLeaderboardLookup = new HashMap<>();
    private final HashMap<String, HashMap<UUID, ParkourLeaderboardEntry>> leaderboardEntries = new HashMap<>();

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
//                TaskSchedule.seconds(30)
                TaskSchedule.seconds(10)
        );
    }

    public void stop(boolean save) {
        if (save) handleQueuedParkourTries();
        if (queueTask == null) return;

        queueTask.cancel();
        queueTask = null;
    }

    public void handleParkourTry(Player player, Parkour parkour, ParkourTry parkourTry) {
        queuedParkourTries.add(parkourTry);
        if (!parkourTry.hasFinished()) return;

        boolean personalBest = updatePersonalBest(player, parkourTry);

        handleFinish(player, parkourTry, personalBest);
        if (personalBest) updateLeaderboard(parkour);
    }

    private void handleFinish(Player player, ParkourTry parkourTry, boolean personalBest) {
        long completionTime = parkourTry.completionTime();
        String time = TextUtils.getFormattedTime(completionTime, true, false);

        if (personalBest) {
            player.sendMessage(TextUtils.miniText("<green>You've achieved a new personal best of <yellow><b>%s</b></yellow>!", time));
            player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1.0f, 1.2f));
        } else {
            player.sendMessage(TextUtils.miniText("<green>You finished the parkour in <yellow><b>%s</b></yellow>!", time));
            player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

    private boolean updatePersonalBest(Player player, ParkourTry parkourTry) {
        HashMap<UUID, ParkourLeaderboardEntry> parkourEntries = leaderboardEntries.computeIfAbsent(parkourTry.parkourId(), k -> new HashMap<>());
        ParkourLeaderboardEntry existingEntry = parkourEntries.get(parkourTry.playerUuid());
        if (existingEntry != null && parkourTry.completionTime() >= existingEntry.completionTime()) return false;

        parkourEntries.put(parkourTry.playerUuid(), new ParkourLeaderboardEntry(
                parkourTry.parkourId(),
                parkourTry.playerUuid(),
                player.getUsername(),
                parkourTry.completionTime()
        ));

        return true;
    }

    private void updateLeaderboard(Parkour parkour) {
        Acquirable<? extends Entity> leaderboardEntityAcquirable = parkour.leaderboardEntity();
        if (leaderboardEntityAcquirable == null) return;

        HashMap<UUID, ParkourLeaderboardEntry> parkourEntries = leaderboardEntries.get(parkour.id());
        if (parkourEntries == null) return;

        List<ParkourLeaderboardEntry> sortedEntries = new ArrayList<>(parkourEntries.values());
        sortedEntries.sort(Comparator.comparingLong(ParkourLeaderboardEntry::completionTime));

        List<ParkourLeaderboardEntry> topEntries = sortedEntries.stream().limit(5).toList();

        leaderboardEntityAcquirable.sync(entity -> {

            Component text = TextUtils.combine(
                    TextUtils.miniText("<#fcba03><b>Leaderboard"),
                    Component.newline(),
                    Component.newline()
            );

            for (int i = 0; i < topEntries.size(); i++) {
                ParkourLeaderboardEntry entry = topEntries.get(i);
                String formattedTime = TextUtils.getFormattedTime(entry.completionTime(), true, true);

                Component line = TextUtils.miniText("<#ffd966>%d. <white>%s <gray>- <yellow>%s",
                        i + 1,
                        entry.playerName(),
                        formattedTime
                );

                text = TextUtils.combine(
                        text,
                        line,
                        Component.newline()
                );
            }

            text = TextUtils.combine(
                    text,
                    Component.newline(),
                    Component.newline(),
                    TextUtils.miniText("<gray><i>Stats are currently <b><red>NOT</red></b> saved!")
            );

            Component finalText = text;
            entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> textDisplayMeta.setText(finalText));
        });
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

                System.out.println("\n======= POST PARKOUR TRY =======");
                System.out.println(json);
                System.out.println("======= END =======\n");

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
