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
import java.util.HashMap;
import java.util.UUID;

public class PackManager {

    private static PackManager INSTANCE;

    private final MsgManager msgManager = MsgManager.getInstance();

    private final UUID resourcePackId = UUID.fromString("674a8d35-73d6-4d6c-b299-d321bd8c5a85");
    private final String resourcePackUrl = "https://maven.pietr.space/snapshots/nl/devpieter/files/packs/PietrLobby-0.1.0+1.21.10.zip";
    private final String resourcePackHash = "";

    private final HashMap<UUID, ResourcePackStatus> playerPackStatuses = new HashMap<>();

    private PackManager() {
    }

    public static PackManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PackManager();
        return INSTANCE;
    }

    public void tryInformRecommendation(@NotNull Player player, boolean required) {
        if (hasAcceptedPack(player)) return;

        Component informComponent  = TextUtils.combine(required
                        ? TextUtils.miniText("<red>This feature only works with the server resource pack enabled.</red>")
                        : TextUtils.miniText("<yellow>This feature works best with the server resource pack enabled.</yellow>"),
                Component.newline(),
                TextUtils.miniText("<gray><i>You can enable the resource pack in your server settings.</i></gray>")
        );

        msgManager.showMessage(player, "resource_pack_recommendation", informComponent, required);
    }

    public void promptResourcePack(@NotNull Player player) {
        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo()
                .id(resourcePackId).uri(URI.create(resourcePackUrl)).hash(resourcePackHash)
                .build();

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .prompt(TextUtils.combine(
                        Component.newline(),
                        TextUtils.miniText("<b><gradient:#6A75AF:#303449>Pietr 🚀 Space</gradient></b> Lobby Resource Pack")
                ))
                .replace(true).required(false)
                .packs(info)
                .build();

        player.sendResourcePacks(request);
    }

    public void updateStatus(@NotNull Player player, ResourcePackStatus status) {
        System.out.println("Resource pack status for player " + player.getUsername() + ": " + status);
        playerPackStatuses.put(player.getUuid(), status);
    }

    public boolean hasAcceptedPack(@NotNull Player player) {
        ResourcePackStatus status = playerPackStatuses.getOrDefault(player.getUuid(), ResourcePackStatus.DECLINED);
        return status == ResourcePackStatus.SUCCESSFULLY_LOADED;
    }
}
