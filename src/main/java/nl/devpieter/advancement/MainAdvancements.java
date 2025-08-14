package nl.devpieter.advancement;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.*;
import net.minestom.server.item.Material;
import nl.devpieter.models.AdvancementHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainAdvancements implements IAdvancements {

    private static final AdvancementManager ADVANCEMENT_MANAGER = MinecraftServer.getAdvancementManager();
    public static final LinkedHashMap<String, AdvancementHolder> ADVANCEMENTS = new LinkedHashMap<>();

    public static AdvancementRoot ROOT_ADVANCEMENT = new AdvancementRoot(
            Component.text("Pietr Space"),
            Component.text("Welcome to Pietr Space!"),
            Material.GRASS_BLOCK,
            FrameType.TASK,
            0, 0,
            ""
    );

    public static AdvancementHolder ROOT = register(
            "pietr_space:advancement.main.root",
            ROOT_ADVANCEMENT
    );

    private static AdvancementTab TAB = ADVANCEMENT_MANAGER.createTab(
            "pietr_space:advancement.main.root",
            (AdvancementRoot) ROOT_ADVANCEMENT
    );

    public static AdvancementHolder JOIN_THE_LOBBY = register(
            "main.join_the_lobby",
            ROOT,
            "Join the Lobby",
            "You have joined the lobby!",
            Material.NETHER_STAR,
            FrameType.TASK,
            1, 0
    );

    public static AdvancementHolder EXPLORE_THE_VOID = register(
            "main.explore_the_void",
            JOIN_THE_LOBBY,
            "Explore the Void",
            "It's so empty down here, isn't it?",
            Material.FEATHER,
            FrameType.CHALLENGE,
            2, 1
    );

    public static AdvancementHolder EXPLORE_THE_NETHER = register(
            "main.explore_the_nether",
            JOIN_THE_LOBBY,
            "Explore the Nether",
            "It's so hot down here, isn't it?",
            Material.NETHER_BRICK,
            FrameType.CHALLENGE,
            2, -1
    );

    @Override
    public AdvancementTab getTab() {
        return TAB;
    }

    @Override
    public HashMap<String, AdvancementHolder> getAdvancements() {
        return ADVANCEMENTS;
    }

    private static @NotNull AdvancementHolder register(String id, @Nullable AdvancementHolder parent, String title, String description, Material icon, FrameType frameType, int x, int y) {
        if (ADVANCEMENTS.containsKey(id)) throw new IllegalArgumentException("Advancement already registered: " + id);

        System.out.println("Registering advancement: " + id);

        AdvancementHolder holder = new AdvancementHolder(id, parent, new Advancement(
                Component.text(title),
                Component.text(description),
                icon,
                frameType,
                x, y
        ));

        holder.advancement().showToast(true);

        ADVANCEMENTS.put(id, holder);
        return holder;
    }

    private static @NotNull AdvancementHolder register(String id, AdvancementRoot root) {
        if (ADVANCEMENTS.containsKey(id)) throw new IllegalArgumentException("Advancement already registered: " + id);

        System.out.println("Registering root advancement: " + id);

        AdvancementHolder holder = new AdvancementHolder(id, null, root);
        ADVANCEMENTS.put(id, holder);
        return holder;
    }
}
