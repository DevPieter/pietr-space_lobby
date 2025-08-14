package nl.devpieter.advancement;

import net.minestom.server.advancements.AdvancementTab;
import nl.devpieter.models.AdvancementHolder;

import java.util.HashMap;

public interface IAdvancements {

    AdvancementTab getTab();

    HashMap<String, AdvancementHolder> getAdvancements();
}
