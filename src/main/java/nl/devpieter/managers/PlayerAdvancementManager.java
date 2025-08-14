package nl.devpieter.managers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.Advancement;
import net.minestom.server.advancements.AdvancementRoot;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.entity.Player;
import nl.devpieter.advancement.MainAdvancements;
import nl.devpieter.models.AdvancementHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerAdvancementManager {

    private final Player player;
    private final AdvancementTab tab;
    private final Map<String, Advancement> personalAdvancements = new HashMap<>();

    public PlayerAdvancementManager(Player player) {
        this.player = player;

        // Create personal root
        AdvancementRoot root = cloneAdvancementRoot(MainAdvancements.ROOT_ADVANCEMENT);
        this.tab = MinecraftServer.getAdvancementManager().createTab(player.getUuid() + ":advancement", root);
        this.tab.addViewer(player);

        // Create per-player children
        for (var entry : MainAdvancements.ADVANCEMENTS.entrySet()) {
            System.out.println("Creating advancement for player: " + player.getUsername() + " - " + entry.getKey());

            String id = entry.getKey();
            Advancement template = entry.getValue().advancement();

            if (template instanceof AdvancementRoot) continue; // root already handled

            Advancement copy = cloneAdvancement(template);
            personalAdvancements.put(id, copy);

            AdvancementHolder parentHolder = entry.getValue().parent();
            if (parentHolder != null) {
                Advancement parentCopy = personalAdvancements.get(parentHolder.id());
                tab.createAdvancement(id, copy, parentCopy);
            }
        }
    }

    public void grant(String id) {
        Advancement adv = personalAdvancements.get(id);
        if (adv != null) adv.setAchieved(true);
    }

    private @NotNull Advancement cloneAdvancement(@NotNull Advancement template) {
        Advancement clone = new Advancement(
                template.getTitle(),
                template.getDescription(),
                template.getIcon(),
                template.getFrameType(),
                template.getX(),
                template.getY()
        );

        clone.showToast(template.hasToast());
        clone.setHidden(template.isHidden());

        return clone;
    }

    private AdvancementRoot cloneAdvancementRoot(AdvancementRoot template) {
        AdvancementRoot clone = new AdvancementRoot(
                template.getTitle(),
                template.getDescription(),
                template.getIcon(),
                template.getFrameType(),
                template.getX(),
                template.getY(),
                "minecraft:gui/advancements/backgrounds/stone"
        );

        clone.showToast(template.hasToast());
        clone.setHidden(template.isHidden());

        return clone;
    }
}