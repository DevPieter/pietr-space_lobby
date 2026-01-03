package nl.devpieter.features.resourcePack;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import nl.devpieter.features.tips.MsgManager;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PackManager {

    private static PackManager INSTANCE;

    private final MsgManager msgManager = MsgManager.getInstance();

    private final UUID resourcePackId = UUID.fromString("674a8d35-73d6-4d6c-b299-d321bd8c5a85");
    private final String resourcePackUrl = "https://maven.pietr.space/snapshots/nl/devpieter/files/packs/PietrLobby-0.1.0+1.21.10.zip";
    private final String resourcePackHash = "";

    private final List<UUID> acceptedPlayers = new ArrayList<>();
    private final List<UUID> promptedPlayers = new ArrayList<>();
    private final HashMap<UUID, ResourcePackStatus> playerPackStatuses = new HashMap<>();

    private PackManager() {
    }

    public static PackManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PackManager();
        return INSTANCE;
    }

    public boolean hasPackAccepted(@NotNull Player player) {
        return acceptedPlayers.contains(player.getUuid());
    }

    public boolean hasPrompted(@NotNull Player player) {
        return promptedPlayers.contains(player.getUuid());
    }

    public boolean hasPackLoaded(@NotNull Player player) {
        ResourcePackStatus status = playerPackStatuses.getOrDefault(player.getUuid(), ResourcePackStatus.DECLINED);
        return status == ResourcePackStatus.SUCCESSFULLY_LOADED || status == ResourcePackStatus.ACCEPTED;
    }

    public boolean canAutoPrompt(@NotNull Player player) {
        return !hasPackLoaded(player) && hasPackAccepted(player);
    }

    public void tryAutoPrompt(@NotNull Player player) {
        if (canAutoPrompt(player)) promptResourcePack(player);
    }

    public void tryInformRecommendation(@NotNull Player player, boolean required) {
        if (hasPackLoaded(player)) return;
        if (!hasPrompted(player)) promptResourcePack(player);

        msgManager.showMessage(
                player,
                "resource_pack_recommendation",
                TextUtils.combine(required
                                ? TextUtils.miniText("<red>This feature only works with the server resource pack enabled.")
                                : TextUtils.miniText("<yellow>This feature works best with the server resource pack enabled."),
                        Component.newline(),
                        TextUtils.miniText("<gray><i>You can enable the resource pack in your server settings.")
                ),
                required
        );
    }

    private void promptResourcePack(@NotNull Player player) {
        if (hasPrompted(player)) return;

        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo()
                .id(resourcePackId).uri(URI.create(resourcePackUrl)).hash(resourcePackHash)
                .build();

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .prompt(TextUtils.combine(
                        Component.newline(),
                        TextUtils.miniText("<#6a75af><b>Pietr 🚀 Space</b>, Full Lobby Experience"),
                        Component.newline(),
                        TextUtils.miniText("<#2b2f40>Enjoy the full lobby experience with our custom resource pack!"),
                        Component.newline(),
                        Component.newline(),
                        TextUtils.miniText("<#2b2f40><i>Some features may not work properly without it.")
                ))
                .replace(true).required(false)
                .packs(info)
                .build();

        player.sendResourcePacks(request.callback((_, status, _) -> updateStatus(player, status)));
        promptedPlayers.add(player.getUuid());
    }

    public void clearStatus(@NotNull Player player) {
        promptedPlayers.remove(player.getUuid());
        playerPackStatuses.remove(player.getUuid());
    }

    private void updateStatus(@NotNull Player player, ResourcePackStatus status) {
        playerPackStatuses.put(player.getUuid(), status);

        boolean hasAccepted = acceptedPlayers.contains(player.getUuid());

        if (!hasAccepted && status == ResourcePackStatus.SUCCESSFULLY_LOADED) {
            acceptedPlayers.add(player.getUuid());

            msgManager.showMessage(
                    player,
                    "resource_pack_accepted",
                    TextUtils.miniText("<green>Thank you for accepting the server resource pack! Enjoy the full experience."),
                    false
            );
        }

        if (hasAccepted && status == ResourcePackStatus.DECLINED) {
            acceptedPlayers.remove(player.getUuid());
        }

        if (status == ResourcePackStatus.FAILED_RELOAD || status == ResourcePackStatus.FAILED_DOWNLOAD) {
            msgManager.showMessage(
                    player,
                    "resource_pack_failed",
                    TextUtils.miniText("<red>Failed to download the server resource pack. Some features may not work properly."),
                    true
            );
        }
    }
}
