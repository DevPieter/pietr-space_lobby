package nl.devpieter.features.tips;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import nl.devpieter.PlayLogger;
import nl.devpieter.features.tips.models.MessageHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.pietr.lobby.common.utils.minestom.SoundUtils;
import space.pietr.lobby.common.utils.minestom.TextUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MsgManager {

    private static MsgManager INSTANCE;

    private static final Duration MESSAGE_COOLDOWN = Duration.ofSeconds(5);

    private final PlayLogger playLogger = PlayLogger.getInstance();

    private final HashMap<UUID, List<MessageHolder>> messages = new HashMap<>();
    private final HashMap<UUID, List<String>> receivedMessages = new HashMap<>();

    private MsgManager() {
    }

    public static MsgManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MsgManager();
        return INSTANCE;
    }

    public void showMessage(@NotNull Player player, @NotNull String id, @NotNull String message) {
        showMessage(player, id, TextUtils.miniText(message));
    }

    public void showMessage(@NotNull Player player, @NotNull String id, @NotNull Component message) {
        showMessage(player, id, message, true);
    }

    public void showMessage(@NotNull Player player, @NotNull String id, @NotNull Component message, boolean sound) {
        removeExpiredMessages(player);

        if (!canShowMessage(player, id)) {
            playLogger.debug(player, true, "Message {} not shown (conditions not met)", id);
            return;
        }

        List<MessageHolder> messageHolders = messages.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>());
        messageHolders.add(new MessageHolder(id, System.currentTimeMillis()));

        receivedMessages.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>()).add(id);

        player.sendMessage(TextUtils.combine(
                message,
                Component.space()
        ));

        if (sound) playSoundEffect(player);
        playLogger.debug(player, true, "Message {} shown (sound: {})", id, sound);
    }

    public void resetMessages(@NotNull Player player) {
        receivedMessages.remove(player.getUuid());
        messages.remove(player.getUuid());
        playLogger.debug(player, true, "Reset messages");
    }

    private boolean canShowMessage(@NotNull Player player, @NotNull String messageId) {
        MessageHolder existing = getMessage(player, messageId);
        if (existing == null) return true;

        long elapsedTime = System.currentTimeMillis() - existing.timestamp();
        return elapsedTime >= MESSAGE_COOLDOWN.toMillis();
    }

    private @Nullable MessageHolder getMessage(@NotNull Player player, @NotNull String messageId) {
        List<MessageHolder> messageHolders = messages.get(player.getUuid());
        if (messageHolders == null) return null;

        for (MessageHolder messageHolder : messageHolders) {
            if (!messageHolder.id().equals(messageId)) continue;
            return messageHolder;
        }

        return null;
    }

    private void removeExpiredMessages(@NotNull Player player) {
        List<MessageHolder> messageHolders = messages.get(player.getUuid());
        if (messageHolders == null) return;

        long currentTime = System.currentTimeMillis();
        messageHolders.removeIf(messageHolder -> (currentTime - messageHolder.timestamp()) >= MESSAGE_COOLDOWN.toMillis());
    }

    private static void playSoundEffect(Player player) {
        SoundUtils.playSoundForPlayer(player, SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }
}
