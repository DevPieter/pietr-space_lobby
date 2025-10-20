package nl.devpieter.features.fishing.rod;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.bobber.IBobber;
import nl.devpieter.features.fishing.FishingManager;
import nl.devpieter.features.fishing.models.BobberSettings;
import nl.devpieter.features.fishing.catchable.ICatchable;
import nl.devpieter.utils.MathUtils;

public interface IRod {

    Component getName();

    ItemStack getItem();

    BobberSettings getBobberSettings();

    void onUse();

    default IBobber castBobber(Player player) {
        return castDefaultBobber(player);
    }

    default FishingBobber castDefaultBobber(Player player) {
        ICatchable catchable = FishingManager.getInstance().getRandomCatchable(player, this);
        FishingBobber bobber = new FishingBobber(player, this, catchable);

        Pos loc = player.getPosition();
        float pitch = loc.pitch();
        float yaw = loc.yaw();

        double yawCos = Math.cos(-yaw * (Math.PI / 180) - Math.PI);
        double yawSin = Math.sin(-yaw * (Math.PI / 180) - Math.PI);

        double pitchCos = -Math.cos(-pitch * (Math.PI / 180));
        double pitchSin = Math.sin(-pitch * (Math.PI / 180));

        double x = loc.x() - yawSin * 0.3;
        double y = loc.y() + player.getEyeHeight();
        double z = loc.z() - yawCos * 0.3;

        bobber.setPosInternal(new Pos(x, y, z, yaw, pitch), 0);

        Vec vec = new Vec(-yawSin, Math.clamp(-(pitchSin / pitchCos), -5.0f, 5.0f), -yawCos);
        double length = vec.length();

        vec = vec.mul(
                0.5 / length + MathUtils.nextTriangular(0.5, 0.0103365),
                0.5 / length + MathUtils.nextTriangular(0.5, 0.0103365),
                0.5 / length + MathUtils.nextTriangular(0.5, 0.0103365)
        );

        bobber.setVelocity(vec);

        bobber.setView(
                (float) (Math.atan2(vec.x(), vec.z()) * (Math.PI / 180)),
                (float) (Math.atan2(vec.y(), vec.length()) * (Math.PI / 180))
        );

        bobber.setInstance(player.getInstance());
        return bobber;
    }
}
