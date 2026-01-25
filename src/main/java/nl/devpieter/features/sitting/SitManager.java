package nl.devpieter.features.sitting;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.thread.Acquirable;
import nl.devpieter.TagHelper;
import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.enums.PlayerTags;
import nl.devpieter.utils.minestom.PlayerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class SitManager {

    private static SitManager INSTANCE;

    private final HashMap<UUID, Acquirable<@NotNull ? extends LivingEntity>> sittingPlayers = new HashMap<>();

    private SitManager() {
    }

    public static SitManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SitManager();
        return INSTANCE;
    }

    public void sitPlayer(@NotNull LobbyPlayer player, Block block, BlockVec blockVec) {
        if (!TagHelper.canSitPlayer(player)) return;
        if (!player.addTag(PlayerTags.SITTING)) return;

        Point bounding = block.registry().collisionShape().relativeEnd();

        LivingEntity entity = new LivingEntity(EntityType.ARMOR_STAND);
        entity.setInstance(player.getInstance(), blockVec.add(0.5, bounding.y() - 1.9, 0.5));
        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.setInvisible(true);
        entity.spawn();

        entity.teleport(entity.getPosition().withYaw(player.getPosition().yaw()).withPitch(player.getPosition().pitch()));
        player.teleport(entity.getPosition().add(0, 1, 0));

        entity.addPassenger(player);
        sittingPlayers.put(player.getUuid(), entity.acquirable());
    }

    public void standPlayer(@NotNull LobbyPlayer player) {
        if (!sittingPlayers.containsKey(player.getUuid())) return;

        Acquirable<? extends LivingEntity> acquirable = sittingPlayers.get(player.getUuid());
        acquirable.sync(livingEntity -> {
            livingEntity.removePassenger(player);
            livingEntity.remove();

            PlayerUtils.teleport(player, player.getPosition().add(0, 1, 0));
            sittingPlayers.remove(player.getUuid());

            player.removeTag(PlayerTags.SITTING);
        });
    }
}
