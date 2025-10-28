package nl.devpieter.listeners;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoadedEvent;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerLoadedListener implements EventListener<@NotNull PlayerLoadedEvent> {

    private final Component welcome = TextUtils.miniText(
            "<gray>Welcome to <b><gradient:#6A75AF:#303449>Pietr 🚀 Space</gradient></b>!\n" +
                    "<gray>Use <yellow>/server</yellow> to switch between worlds."
    );

    @Override
    public @NotNull Class<PlayerLoadedEvent> eventType() {
        return PlayerLoadedEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerLoadedEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(this.welcome);

        return Result.SUCCESS;
    }
}
