package nl.devpieter.features.sitting;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.thread.Acquirable;
import nl.devpieter.TagHelper;
import space.pietr.lobby.common.player.LobbyPlayer;
import space.pietr.lobby.common.player.enums.PlayerTags;
import nl.devpieter.features.tips.MsgManager;
import nl.devpieter.features.world.WorldManager;
import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.features.world.LoadedWorld;
import space.pietr.lobby.common.utils.minestom.PlayerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SitManager {

    private static SitManager INSTANCE;

    private final WorldManager worldManager = WorldManager.getInstance();
    private final MsgManager msgManager = MsgManager.getInstance();

    private final List<String> disallowedBlocks = List.of(
            "minecraft:barrier",
            "minecraft:bedrock",
            "_door",
            "_fence",
            "_sign",
            "lantern",
            "torch"
    );

    private final List<String> exceptionBlocks = List.of(
            "_carpet",
            "_bed",
            "_coral_fan",
            "minecraft:lily_pad",
            "minecraft:azalea",
            "minecraft:flowering_azalea"
    );

    private final HashMap<UUID, Acquirable<@NotNull ? extends LivingEntity>> sittingPlayers = new HashMap<>();

    private SitManager() {
    }

    public static SitManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SitManager();
        return INSTANCE;
    }

    public void trySitPlayer(@NotNull LobbyPlayer player, Block block, BlockVec blockVec) {
        LoadedWorld loadedWorld = worldManager.getLoadedWorldByInstance(player.getInstance());
        if (loadedWorld == null || !loadedWorld.features().sitting().enabled()) return;

        if (!TagHelper.canSitPlayer(player)) return;

        if (!canSitOnBlock(player, block, blockVec)) {
            msgManager.showMessage(player, "cannot_sit_on_block", "<red>You cannot sit on this block!");
            return;
        }

        if (!player.addTag(PlayerTags.SITTING)) return;

        Point bounding = block.collisionShape().relativeEnd();

        LivingEntity entity = new LivingEntity(EntityType.ARMOR_STAND);
        entity.setInstance(player.getInstance(), blockVec.add(0.5, bounding.y() - 1.9, 0.5));
        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.setInvisible(true);
//        entity.spawn();

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

    private boolean canSitOnBlock(@NotNull LobbyPlayer player, Block block, BlockVec blockVec) {
        String key = block.key().asString();

        for (String disallowed : disallowedBlocks) {
            if (key.contains(disallowed)) return false;
        }

        Block blockAbove = player.getInstance().getBlock(blockVec.add(0, 1, 0));
        if (blockAbove != Block.AIR) return false;

        for (String exception : exceptionBlocks) {
            if (key.contains(exception)) return true;
        }

        if (block.compare(Block.CAMPFIRE) || block.compare(Block.SOUL_CAMPFIRE)) {
            String lit = block.getProperty("lit");
            if (lit == null || lit.equals("true")) return false;
        }

        if (key.contains("_trapdoor")) {
            String open = block.getProperty("open");
            if (open == null || open.equals("true")) return false;
        }

        return block.solid();
    }
}
