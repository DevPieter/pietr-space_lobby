package nl.devpieter.features.jukebox.blockHandlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import nl.devpieter.features.jukebox.models.Album;
import nl.devpieter.features.jukebox.models.Artist;
import nl.devpieter.features.jukebox.models.JukeboxState;
import nl.devpieter.features.jukebox.models.Song;
import nl.devpieter.features.resourcePack.PackManager;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JukeboxBlockHandler implements BlockHandler {

    private final PackManager packManager = PackManager.getInstance();

    private final List<Artist> artists = new ArrayList<>();
    private final HashMap<PointHolder, JukeboxState> states = new HashMap<>();

    // TODO - Replace with a better system
    private long lastInventoryClickTime = 0;

    public JukeboxBlockHandler() {

        artists.add(new Artist(
                "Joost Klein",
                List.of(new Album(
                        "Kleinkunst",
                        List.of(
                                new Song("Full Circle", Key.key("lobby", "music.joost.full-circle"), /* 00:01:35 */ 95000),
                                new Song("Joost Klein 5", Key.key("lobby", "music.joost.joost-klein-5"), /* 00:01:35 */ 95000)
                        )
                ))
        ));

//        Artist sefa = new Artist("Sefa");
//        songs.put(sefa, List.of(
//                new Song("Dear God", Key.key("lobby", "music.sefa.dear-god"), 0),
//                new Song("In De Hemel", Key.key("lobby", "music.sefa.in-de-hemel"), 0)
//        ));
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

        packManager.tryInformRecommendation(player, true);
        if (!packManager.hasPackLoaded(player)) return true;

        Inventory artistsInventory = createArtistsInventory(interaction);
        player.openInventory(artistsInventory);

        return true;
    }

    @Override
    public void tick(Tick tick) {
        Instance instance = tick.getInstance();
        Point point = tick.getBlockPosition();

        PointHolder holder = new PointHolder(instance, point);

        JukeboxState state = getState(holder, false);
        if (state != null) state.tick(holder);
    }

    private Inventory createArtistsInventory(@NotNull Interaction interaction) {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, TextUtils.miniText("Artists"));

        // 七七七七七七七七
        // 2 = 七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七
//        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ㇺ七ㇻ七ㇺ"));
//        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ऄ七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七इ七七अ七आ"));

//        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ऄ七अ七आ"));

//        for (Artist artist : songs.keySet()) {
//
//            int songCount = songs.get(artist).size();
//
//            ItemStack artistItem = ItemStack.of(Material.PAPER).with(builder -> {
//                builder.customName(TextUtils.miniText(artist.name()));
//                builder.lore(TextUtils.miniText("<gray>%d song%s available", songCount, songCount != 1 ? "s" : ""));
//            });
//
//            inventory.addItemStack(artistItem);
//        }

        for (Artist artist : artists) {
            int albumCount = artist.albums().size();
            int songCount = artist.albums().stream().mapToInt(album -> album.songs().size()).sum();

            ItemStack artistItem = ItemStack.of(Material.PAPER).with(builder -> {
                builder.customName(TextUtils.miniText(artist.name()));
                builder.lore(TextUtils.miniText("<gray>%d song%s in %d album%s available",
                        songCount, songCount != 1 ? "s" : "",
                        albumCount, albumCount != 1 ? "s" : ""
                ));
            });

            inventory.addItemStack(artistItem);
        }

        ItemStack stopPlayingItem = ItemStack.of(Material.BARRIER).with(builder -> {
            builder.customName(TextUtils.miniText("<red>Stop Playing"));
            builder.lore(TextUtils.miniText("<gray>Stop the music playing from this jukebox"));
        });

        inventory.setItemStack(22, stopPlayingItem);

        inventory.eventNode().addChild(EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv).addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);

            if (System.currentTimeMillis() - lastInventoryClickTime < 500) return;
            lastInventoryClickTime = System.currentTimeMillis();

            Player player = event.getPlayer();
            int slot = event.getSlot();

            if (slot == 22) {
                PointHolder pointHolder = new PointHolder(
                        interaction.getInstance(),
                        interaction.getBlockPosition()
                );

                JukeboxState state = getState(pointHolder, false);
                if (state != null) state.stop(pointHolder, stopped -> {
                    if (stopped) player.sendMessage(TextUtils.miniText("<green>Music stopped."));
                    else player.sendMessage(TextUtils.miniText("<yellow>No music was playing."));
                });

                return;
            }

            if (slot < 0 || slot >= artists.size()) return;

            Inventory albumsInventory = createAlbumsInventory(interaction, artists.get(slot));
            player.openInventory(albumsInventory);

//                    if (slot < 0 || slot >= songs.size()) return;
//
//                    Artist selectedArtist = (Artist) songs.keySet().toArray()[slot];
//                    List<Song> artistSongs = songs.get(selectedArtist);
//
//                    Inventory songsInventory = createSongsInventory(interaction, selectedArtist, artistSongs);
//                    player.openInventory(songsInventory);
        }));
        return inventory;
    }

    private Inventory createAlbumsInventory(@NotNull Interaction interaction, @NotNull Artist artist) {
        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText(artist.name() + "'s Albums"));

        for (Album album : artist.albums()) {
            int songCount = album.songs().size();

            ItemStack albumItem = ItemStack.of(Material.BOOK).with(builder -> {
                builder.customName(TextUtils.miniText(album.name()));
                builder.lore(TextUtils.miniText("<gray>%d song%s available", songCount, songCount != 1 ? "s" : ""));
            });

            inventory.addItemStack(albumItem);
        }

        inventory.eventNode().addChild(EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv).addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);

            if (System.currentTimeMillis() - lastInventoryClickTime < 500) return;
            lastInventoryClickTime = System.currentTimeMillis();

            int slot = event.getSlot();
            if (slot < 0 || slot >= artist.albums().size()) return;

            Album selectedAlbum = artist.albums().get(slot);
            List<Song> albumSongs = selectedAlbum.songs();

            Inventory songsInventory = createSongsInventory(interaction, artist, albumSongs);
            event.getPlayer().openInventory(songsInventory);
        }));

        return inventory;
    }

    private Inventory createSongsInventory(@NotNull Interaction interaction, @NotNull Artist artist, @NotNull List<Song> artistSongs) {
//        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText("<white>七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七ऄ七अ七आ"));
        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, TextUtils.miniText(artist.name() + "'s Songs"));

        for (Song song : artistSongs) {
            ItemStack songItem = ItemStack.of(Material.MUSIC_DISC_13).with(builder -> {
                builder.customName(TextUtils.miniText(song.title()));
                builder.lore(TextUtils.miniText("<gray>Play this song"));
            });

            inventory.addItemStack(songItem);
        }

        inventory.eventNode().addChild(EventNode.type("click", EventFilter.INVENTORY, (event, inv) -> inventory == inv).addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);

            if (System.currentTimeMillis() - lastInventoryClickTime < 500) return;
            lastInventoryClickTime = System.currentTimeMillis();

            int slot = event.getSlot();
            if (slot < 0 || slot >= artistSongs.size()) return;

            PointHolder pointHolder = new PointHolder(
                    interaction.getInstance(),
                    interaction.getBlockPosition()
            );

            JukeboxState state = getState(pointHolder, true);
            if (state != null) state.play(pointHolder, artistSongs.get(slot), (success, queued) -> {
                Player player = event.getPlayer();

                if (!success) {
                    player.sendMessage(TextUtils.miniText("<red>The song could not be played. The queue is full."));
                    return;
                }

                if (queued) player.sendMessage(TextUtils.miniText("<green>Song has been added to the queue."));
                else player.sendMessage(TextUtils.miniText("<green>Now playing song."));
            });
        }));
        return inventory;
    }

    private @Nullable JukeboxState getState(PointHolder pointHolder, boolean createIfAbsent) {
        if (createIfAbsent) return states.computeIfAbsent(pointHolder, _ -> new JukeboxState(pointHolder));
        else return states.get(pointHolder);
    }
}
