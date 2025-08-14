package nl.devpieter.managers;

import net.minestom.server.advancements.Advancement;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.entity.Player;
import nl.devpieter.advancement.IAdvancements;
import nl.devpieter.advancement.MainAdvancements;
import nl.devpieter.models.AdvancementHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class AdvancementService {

//    private static AdvancementService INSTANCE;
//
//    private final HashMap<UUID, HashSet<String>> playerProgress = new HashMap<>();
//
//    private AdvancementService() {
//    }
//
//    public static AdvancementService getInstance() {
//        if (INSTANCE == null) INSTANCE = new AdvancementService();
//        return INSTANCE;
//    }
//
//    public void loadPlayer(Player player, IAdvancements adv) {
//        AdvancementTab tab = adv.getTab();
//        HashMap<String, AdvancementHolder> advancements = adv.getAdvancements();
//
//        tab.addViewer()
//
//        for (Map.Entry<String, AdvancementHolder> pair : advancements.entrySet()) {
//            AdvancementHolder advancementHolder = pair.getValue();
//            Advancement advancement = advancementHolder.advancement();
//
//            if (advancement != null) {
//                player.addAdvancement(advancement);
//                player.sendAdvancementTab(tab);
//            }
//        }
//    }
//
//    private @NotNull Advancement cloneAdvancement(@NotNull Advancement advancement) {
//        Advancement clone = new Advancement(
//                advancement.getTitle(),
//                advancement.getDescription(),
//                advancement.getIcon(),
//                advancement.getFrameType(),
//                advancement.getX(),
//                advancement.getY()
//        );
//
//        clone.showToast(advancement.hasToast());
//        clone.setHidden(advancement.isHidden());
//
//        return clone;
//    }
}
