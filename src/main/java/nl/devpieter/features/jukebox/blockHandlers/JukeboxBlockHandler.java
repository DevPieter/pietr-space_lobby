package nl.devpieter.features.jukebox.blockHandlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.thread.Acquirable;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.minestom.ParticleUtils;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class JukeboxBlockHandler implements BlockHandler {

    record Artist(
            String name
    ) {
    }

    record Song(
            String title,
            Key musicKey,
            long durationMillis
    ) {
    }

    record PlayingSong(
            Song song,
            Sound sound,
            long startTimeMs
    ) {
    }

    class JukeboxState {

        private final Queue<Song> songQueue = new LinkedList<>();

        private PlayingSong currentSong;

        private long lastDisplayTimeMs = 0;
        private long lastParticleTickMs = 0;

        private Acquirable<@NotNull ? extends Entity> displayEntityAcquirable;

        public JukeboxState(PointHolder pointHolder) {

//            Entity entity = new Entity(EntityType.TEXT_DISPLAY);
//            entity.setInstance(instance, leaderboardPos);
//
//            entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
//                textDisplayMeta.setText(text);
//                textDisplayMeta.setScale(new Vec(0.9, 0.9, 0.9));
//
//                textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
//                textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
//                textDisplayMeta.setHasNoGravity(true);
//            });

            Entity entity = new Entity(EntityType.TEXT_DISPLAY);
            entity.setInstance(pointHolder.instance(), pointHolder.position().add(0.5, 1.5, 0.5));

            entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
                textDisplayMeta.setText(TextUtils.combine(
                        TextUtils.miniText("<white>Jukebox"),
                        Component.newline(),
                        TextUtils.miniText("<gray>No music playing")
                ));
                textDisplayMeta.setScale(new Vec(0.4, 0.4, 0.4));

                textDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                textDisplayMeta.setAlignment(TextDisplayMeta.Alignment.CENTER);
                textDisplayMeta.setHasNoGravity(true);
            });

            this.displayEntityAcquirable = entity.acquirable();
        }

        public PlayingSong currentSong() {
            return currentSong;
        }

        public boolean isPlaying() {
            if (currentSong == null) return false;
            return System.currentTimeMillis() - currentSong.startTimeMs() < currentSong.song().durationMillis();
        }

        public boolean hasQueuedSongs() {
            return !songQueue.isEmpty();
        }

        public void enqueueSong(Song song) {
            songQueue.add(song);
        }

        public Song dequeueSong() {
            return songQueue.poll();
        }

        public void setCurrentSong(PlayingSong currentSong) {
            this.currentSong = currentSong;
        }

        public void tick(PointHolder pointHolder) {
            tickEntity(pointHolder);
            tickParticles(pointHolder);
        }

        private void tickEntity(PointHolder pointHolder) {
            if (displayEntityAcquirable == null) return;

            long currentTimeMs = System.currentTimeMillis();
            if (currentTimeMs - lastDisplayTimeMs < 100) return;

            lastDisplayTimeMs = currentTimeMs;

            displayEntityAcquirable.sync(entity -> {
                entity.editEntityMeta(TextDisplayMeta.class, textDisplayMeta -> {
                    if (isPlaying()) {
                        Song song = currentSong.song();
                        long elapsedTime = System.currentTimeMillis() - currentSong.startTimeMs();
                        textDisplayMeta.setText(TextUtils.combine(
                                TextUtils.miniText("<white>Jukebox"),
                                Component.newline(),
                                TextUtils.miniText("<gray>Playing: <yellow>%s", song.title()),
                                Component.newline(),
                                TextUtils.miniText("<gray>%s / %s",
                                        TextUtils.getFormattedTime(elapsedTime, false, true),
                                        TextUtils.getFormattedTime(song.durationMillis(), false, true)
                                )
                        ));
                    } else {
                        textDisplayMeta.setText(TextUtils.combine(
                                TextUtils.miniText("<white>Jukebox"),
                                Component.newline(),
                                TextUtils.miniText("<gray>No music playing")
                        ));
                    }
                });
            });
        }


        private void tickParticles(PointHolder pointHolder) {
            if (!isPlaying()) return;

            long currentTimeMs = System.currentTimeMillis();
            if (currentTimeMs - lastParticleTickMs < 800) return;

            lastParticleTickMs = currentTimeMs;

            ParticleUtils.spawnParticleToViewers(
                    pointHolder.instance(),
                    Particle.NOTE,
                    pointHolder.position().add(0.5, 1.4, 0.5),
                    new Vec(0.2, 0.3, 0.2),
                    0.1f,
                    3
            );
        }
    }

    private final PackManager packManager = PackManager.getInstance();
    private final HashMap<Artist, List<Song>> songs = new HashMap<>();

    private final HashMap<PointHolder, JukeboxState> states = new HashMap<>();

    public JukeboxBlockHandler() {
        Artist joost = new Artist("Joost Klein");
        songs.put(joost, List.of(
                new Song("Full Circle", Key.key("lobby", "music.joost.full-circle"), /* 00:01:35 */ 95000),
                new Song("Joost Klein 5", Key.key("lobby", "music.joost.joost-klein-5"), /* 00:01:35 */ 95000)
        ));

        Artist sefa = new Artist("Sefa");
        songs.put(sefa, List.of(
                new Song("Dear God", Key.key("lobby", "music.sefa.dear-god"), 0),
                new Song("In De Hemel", Key.key("lobby", "music.sefa.in-de-hemel"), 0)
        ));

//        for (int i = 1; i <= 10; i++) {
//            Artist artist = new Artist("Artist " + i);
//
//            songs.put(artist, List.of(
//                    new Song("Song A" + i, Key.key("lobby", "music.artist" + i + ".song-a"), 0),
//                    new Song("Song B" + i, Key.key("lobby", "music.artist" + i + ".song-b"), 0),
//                    new Song("Song C" + i, Key.key("lobby", "music.artist" + i + ".song-c"), 0)
//            ));
//        }
    }

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
        packManager.tryInformRecommendation(player, false);

        Inventory artistsInventory = createArtistsInventory(interaction);
        player.openInventory(artistsInventory);

        return true;
    }

    @Override
    public void tick(Tick tick) {
        Instance instance = tick.getInstance();
        Point point = tick.getBlockPosition();
        PointHolder holder = new PointHolder(instance, point);

        JukeboxState state = states.get(holder);
        if (state == null) return;
        state.tick(holder);

        PlayingSong playingSong = state.currentSong();
        if (playingSong == null) return;

        Song song = playingSong.song();
        if (song == null) return;

        long soundDuration = song.durationMillis();
        if (soundDuration <= 0) return;

        long elapsedTime = System.currentTimeMillis() - playingSong.startTimeMs();
        if (elapsedTime >= soundDuration) {
            instance.stopSound(playingSong.sound());
//            state.setCurrentSong(null);

            if (state.hasQueuedSongs()) {
                Song nextSong = state.dequeueSong();
                playMusic(holder, nextSong);
                System.out.println("Playing next song at jukebox at " + point);
                return;
            }

            System.out.println("Stopped music at jukebox at " + point);
            state.setCurrentSong(null);
            return;
        }
    }

    private void playMusic(@NotNull PointHolder pointHolder, @NotNull JukeboxBlockHandler.Song record) {
//        Instance instance = interaction.getPlayer().getInstance();
//        Point point = interaction.getBlockPosition().add(0.5, 0.5, 0.5);

        Sound sound = Sound.sound(
                record.musicKey(),
                Sound.Source.RECORD,
                1.0f,
                1.0f
        );

        pointHolder.instance().playSound(sound, pointHolder.position().add(0.5, 0.5, 0.5));
        JukeboxState state = states.computeIfAbsent(pointHolder, k -> new JukeboxState(pointHolder));

        state.setCurrentSong(new PlayingSong(
                record,
                sound,
                System.currentTimeMillis()
        ));
    }

    private void stopMusic(@NotNull PointHolder pointHolder) {
        JukeboxState state = states.get(pointHolder);
        if (state == null) return;

        PlayingSong playingSong = state.currentSong();
        if (playingSong == null) return;

        pointHolder.instance().stopSound(playingSong.sound());
        state.setCurrentSong(null);
    }

    private Inventory createArtistsInventory(@NotNull Interaction interaction) {
//        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Artists"));

        // 七七七七七七七七
        // 2 = 七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七
//        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ㇺ七ㇻ七ㇺ"));
        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ऄ七अ七आ"));
//        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ऄ七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七इ七七अ七आ"));

        for (Artist artist : songs.keySet()) {

            int songCount = songs.get(artist).size();

            ItemStack artistItem = ItemStack.of(Material.PAPER).with(builder -> {
                builder.customName(TextUtils.miniText(artist.name()));
                builder.lore(TextUtils.miniText("<gray>%d song%s available", songCount, songCount != 1 ? "s" : ""));
            });

            inventory.addItemStack(artistItem);
        }

        var test = EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv)
                .addListener(InventoryPreClickEvent.class, event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    int slot = event.getSlot();

                    if (slot < 0 || slot >= songs.size()) return;

                    Artist selectedArtist = (Artist) songs.keySet().toArray()[slot];
                    List<Song> artistSongs = songs.get(selectedArtist);

                    Inventory songsInventory = createSongsInventory(interaction, selectedArtist, artistSongs);
                    player.openInventory(songsInventory);
                });

        inventory.eventNode().addChild(test);

        return inventory;
    }

    private Inventory createSongsInventory(@NotNull Interaction interaction, @NotNull Artist artist, @NotNull List<Song> artistSongs) {
        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ऄ七अ七आ"));

        for (Song song : artistSongs) {
            ItemStack songItem = ItemStack.of(Material.MUSIC_DISC_13).with(builder -> {
                builder.customName(TextUtils.miniText(song.title()));
                builder.lore(TextUtils.miniText("<gray>Play this song"));
            });

            inventory.addItemStack(songItem);
        }

        var test = EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv)
                .addListener(InventoryPreClickEvent.class, event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    int slot = event.getSlot();

                    if (slot < 0 || slot >= artistSongs.size()) return;
//                    player.closeInventory();

//                    stopMusic(interaction);
//                    playMusic(interaction, artistSongs.get(slot));

                    PointHolder pointHolder = new PointHolder(
                            interaction.getInstance(),
                            interaction.getBlockPosition()
                    );

//                    stopMusic(pointHolder);
//                    playMusic(pointHolder, artistSongs.get(slot));

                    JukeboxState state = states.get(pointHolder);
                    if (state == null) {
                        playMusic(pointHolder, artistSongs.get(slot));
                        System.out.println("Playing music at jukebox at " + interaction.getBlockPosition());
                    } else {
                        if (state.isPlaying()) {
                            state.enqueueSong(artistSongs.get(slot));
                            System.out.println("Queued song at jukebox at " + interaction.getBlockPosition());
                        } else {
                            playMusic(pointHolder, artistSongs.get(slot));
                            System.out.println("Playing music at jukebox at " + interaction.getBlockPosition());
                        }
                    }
                });

        inventory.eventNode().addChild(test);
        return inventory;
    }
}
