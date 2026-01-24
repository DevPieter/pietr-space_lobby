package nl.devpieter.features.resourcePack;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import nl.devpieter.features.resourcePack.models.PackStatus;
import nl.devpieter.features.resourcePack.models.ResourcePack;
import nl.devpieter.features.tips.MsgManager;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PackManager {

    private static PackManager INSTANCE;

    public static final UUID MAIN_PACK = UUID.fromString("674a8d35-73d6-4d6c-b299-d321bd8c5a85");

    private final MsgManager msgManager = MsgManager.getInstance();

    private final HashMap<UUID, List<UUID>> acceptedPlayers = new HashMap<>();
    private final HashMap<UUID, List<UUID>> promptedPlayers = new HashMap<>();
    private final HashMap<UUID, List<PackStatus>> playerPackStatuses = new HashMap<>();

    private final HashMap<UUID, ResourcePack> resourcePacks = new HashMap<>();

    private PackManager() {
        ResourcePack mainPack = new ResourcePack(
                MAIN_PACK,
                "https://maven.pietr.space/snapshots/nl/devpieter/files/packs/PietrLobby-0.1.1+1.21.10.zip",
                "",
                TextUtils.miniText("<#6a75af><b>Pietr 🚀 Space</b>, Full Lobby Experience"),
                TextUtils.combine(
                        Component.newline(),
                        TextUtils.miniText("<#2b2f40>Enjoy the full lobby experience with our custom resource pack!"),
                        Component.newline(),
                        Component.newline(),
                        TextUtils.miniText("<#2b2f40><i>Some features may not work properly without it.")
                ),
                true,
                true
        );

        resourcePacks.put(mainPack.packId(), mainPack);
    }

    public static PackManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PackManager();
        return INSTANCE;
    }

    public void registerResourcePack(@NotNull ResourcePack resourcePack) {
        resourcePacks.put(resourcePack.packId(), resourcePack);
    }

    public boolean hasPackAccepted(@NotNull Player player, UUID packId) {
        List<UUID> acceptedPacks = acceptedPlayers.get(player.getUuid());
        if (acceptedPacks == null) return false;

        return acceptedPacks.contains(packId);
    }

    public boolean hasPrompted(@NotNull Player player, UUID packId) {
        List<UUID> promptedPacks = promptedPlayers.get(player.getUuid());
        if (promptedPacks == null) return false;

        return promptedPacks.contains(packId);
    }

    public boolean hasPackLoaded(@NotNull Player player, UUID packId) {
        PackStatus status = getPackStatus(player, packId);
        if (status == null) return false;

        return status.status() == ResourcePackStatus.SUCCESSFULLY_LOADED || status.status() == ResourcePackStatus.ACCEPTED;
    }

    public boolean canAutoPrompt(@NotNull Player player, UUID packId) {
        if (hasPackLoaded(player, packId)) return false;
        if (!hasPackAccepted(player, packId)) return false;

        ResourcePack pack = resourcePacks.get(packId);
        if (pack == null) return false;

        return pack.autoPrompt();
    }

    public void tryAutoPrompt(@NotNull Player player, UUID packId) {
        if (canAutoPrompt(player, packId)) promptResourcePack(player, packId);
    }

    public void tryInformRecommendation(@NotNull Player player, UUID packId, boolean required) {
        if (hasPackLoaded(player, packId)) return;
        if (!hasPrompted(player, packId)) promptResourcePack(player, packId);

        ResourcePack pack = resourcePacks.get(packId);
        if (pack == null) return;

        msgManager.showMessage(
                player,
                "resource_pack_recommendation",
                TextUtils.combine(required ?
                                TextUtils.combine(
                                        TextUtils.miniText("<red>This feature only works with the"),
                                        Component.space(),
                                        pack.displayName(),
                                        Component.space(), TextUtils.miniText("<red>pack enabled.")
                                ) :
                                TextUtils.combine(
                                        TextUtils.miniText("<yellow>This feature works best with the"),
                                        Component.space(),
                                        pack.displayName(),
                                        Component.space(), TextUtils.miniText("<yellow>pack enabled.")
                                ),
                        Component.newline(),
                        TextUtils.miniText("<gray><i>You can accept server packs in your server settings.")
                ),
                required
        );
    }

    private void promptResourcePack(@NotNull Player player, UUID packId) {
        ResourcePack pack = resourcePacks.get(packId);
        if (pack == null) return;

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .prompt(pack.promptMessage())
                .replace(true).required(false)
                .packs(pack.toPackInfo(), resourcePacks.get(MAIN_PACK).toPackInfo()) // TODO - Add all currently loaded packs
                .build();

        player.sendResourcePacks(request.callback(this::updateStatus));
        markPrompted(player.getUuid(), packId);
    }

    public void clearStatus(@NotNull Player player) {
        promptedPlayers.remove(player.getUuid());
        playerPackStatuses.remove(player.getUuid());
    }

    private void updateStatus(@NotNull UUID packId, @NotNull ResourcePackStatus status, @NotNull Audience audience) {
        if (!(audience instanceof Player player)) return;

        System.out.println("Resource Pack Status for " + player.getUsername() + ": " + status.name() + " (Pack ID: " + packId + ")");

        List<PackStatus> statuses = playerPackStatuses.getOrDefault(player.getUuid(), new ArrayList<>());

        PackStatus existingStatus = getPackStatus(player, packId);
        if (existingStatus != null) statuses.remove(existingStatus);

        statuses.add(new PackStatus(packId, status));
        playerPackStatuses.put(player.getUuid(), statuses);

        if (status == ResourcePackStatus.ACCEPTED) markAccepted(player.getUuid(), packId, true);
        else if (status == ResourcePackStatus.DECLINED) markAccepted(player.getUuid(), packId, false);
    }

    private void markPrompted(@NotNull UUID playerId, UUID packId) {
        List<UUID> promptedPacks = promptedPlayers.getOrDefault(playerId, new ArrayList<>());
        if (promptedPacks.contains(packId)) return;

        promptedPacks.add(packId);
        promptedPlayers.put(playerId, promptedPacks);
    }

    private void markAccepted(@NotNull UUID playerId, UUID packId, boolean accepted) {
        List<UUID> acceptedPacks = acceptedPlayers.getOrDefault(playerId, new ArrayList<>());

        if (accepted) {
            if (!acceptedPacks.contains(packId)) {
                acceptedPacks.add(packId);
                acceptedPlayers.put(playerId, acceptedPacks);
            }
        } else {
            if (acceptedPacks.contains(packId)) {
                acceptedPacks.remove(packId);
                acceptedPlayers.put(playerId, acceptedPacks);
            }
        }
    }

    private @Nullable PackStatus getPackStatus(@NotNull Player player, UUID packId) {
        List<PackStatus> statuses = playerPackStatuses.get(player.getUuid());
        if (statuses == null) return null;

        for (PackStatus status : statuses) {
            if (!status.packId().equals(packId)) continue;
            return status;
        }

        return null;
    }
}
