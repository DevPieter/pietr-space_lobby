package nl.devpieter.features.states;

import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import nl.devpieter.features.states.enums.PlayerTags;
import nl.devpieter.utils.minestom.PlayerUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerStateManager {

    private static PlayerStateManager INSTANCE;

    private PlayerStateManager() {
    }

    public static PlayerStateManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PlayerStateManager();
        return INSTANCE;
    }

    public boolean get(Player player, PlayerTags tag) {
        return get(player, tag.tag());
    }

    public boolean get(Player player, String tag) {
        Tag<@NotNull Boolean> bTag = Tag.Boolean(tag);
        return Boolean.TRUE.equals(PlayerUtils.readPlayerTag(player, bTag));
    }

    public boolean set(Player player, PlayerTags tag) {
        return set(player, tag.tag());
    }

    public boolean set(Player player, String tag) {
        Tag<@NotNull Boolean> bTag = Tag.Boolean(tag);
        return PlayerUtils.writePlayerTag(player, bTag, true);
    }

    public boolean remove(Player player, PlayerTags tag) {
        return remove(player, tag.tag());
    }

    public boolean remove(Player player, String tag) {
        Tag<@NotNull Boolean> bTag = Tag.Boolean(tag);
        return PlayerUtils.removePlayerTag(player, bTag);
    }
}
