package nl.devpieter.managers;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class SitManager {

    private static SitManager INSTANCE;

    private final ParkourManager parkourManager = ParkourManager.getInstance();

    private final HashMap<UUID, Entity> sittingPlayers = new HashMap<>();

    private SitManager() {
    }

    public static SitManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SitManager();
        return INSTANCE;
    }

    public void sitPlayer(@NotNull Player player, Block block, BlockVec blockVec) {
        if (this.sittingPlayers.containsKey(player.getUuid())) return;

        if (this.parkourManager.isDoingParkour(player)) {
            player.sendMessage(TextUtils.miniText("<red>You cannot sit while doing a parkour!"));
            return;
        }

        Point bounding = block.registry().collisionShape().relativeEnd();

        LivingEntity entity = new LivingEntity(EntityType.ARMOR_STAND);
        entity.setInstance(player.getInstance(), blockVec.asVec().add(0.5, bounding.y() - 1.9, 0.5));
        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.setInvisible(true);
        entity.spawn();

        entity.teleport(entity.getPosition().withYaw(player.getPosition().yaw()).withPitch(player.getPosition().pitch()));
        player.teleport(entity.getPosition().add(0, 1, 0));

        entity.addPassenger(player);
        this.sittingPlayers.put(player.getUuid(), entity);
    }

    public void standPlayer(@NotNull Player player) {
        if (!this.sittingPlayers.containsKey(player.getUuid())) return;

        Entity entity = this.sittingPlayers.get(player.getUuid());
        entity.removePassenger(player);
        entity.remove();

        player.teleport(player.getPosition().add(0, 1, 0));
        this.sittingPlayers.remove(player.getUuid());
    }
}
