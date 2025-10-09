package nl.devpieter.listeners;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class PlayerUseItemListener implements EventListener<@NotNull PlayerUseItemEvent> {

    private final HashMap<UUID, EntityCreature> bobbers = new HashMap<>();

    @Override
    public @NotNull Class<PlayerUseItemEvent> eventType() {
        return PlayerUseItemEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerUseItemEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != PlayerHand.MAIN) return Result.SUCCESS;

        if (event.getItemStack().material() == Material.FISHING_ROD) {
            if (bobbers.containsKey(player.getUuid())) reelInFishingRod(player);
            else castFishingRod(player);
        }

        return Result.SUCCESS;
    }

    private void castFishingRod(Player player) {
        EntityCreature bobber = new EntityCreature(EntityType.FISHING_BOBBER);
        FishingHookMeta meta = (FishingHookMeta) bobber.getEntityMeta();
        meta.setOwnerEntity(player);

        bobbers.put(player.getUuid(), bobber);

        Pos playerPos = player.getPosition();
        float playerPitch = playerPos.pitch();
        float playerYaw = playerPos.yaw();

        float zDir = (float) Math.cos(Math.toRadians(-playerYaw) - Math.PI);
        float xDir = (float) Math.sin(Math.toRadians(-playerYaw) - Math.PI);

        double x = playerPos.x() - (double) xDir * 0.3D;
        double y = playerPos.y() + player.getEyeHeight();
        double z = playerPos.z() - (double) zDir * 0.3D;

        bobber.setInstance(player.getInstance(), new Pos(x, y, z));

        Vec velocity = new Vec(
                -xDir,
                MathUtils.clamp(-((float) Math.sin(Math.toRadians(-playerPitch)) / (float) -Math.cos(Math.toRadians(-playerPitch))), -5.0F, 5.0F),
                -zDir
        );

        bobber.setVelocity(velocity.mul(ServerFlag.SERVER_TICKS_PER_SECOND * 0.75));
    }

    private void reelInFishingRod(Player player) {
        EntityCreature bobber = bobbers.remove(player.getUuid());
        if (bobber != null) bobber.remove();
    }
}
