package nl.devpieter.features.fishing.rod;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import nl.devpieter.Settings;
import nl.devpieter.features.fishing.bobber.FishingBobber;
import nl.devpieter.features.fishing.bobber.IBobber;
import nl.devpieter.features.fishing.models.BobberSettings;
import nl.devpieter.utils.MathUtils;
import nl.devpieter.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Rod implements IRod {

    @Override
    public ItemStack buildItem() {
        List<Component> lore = new ArrayList<>(List.of(
                TextUtils.miniText("<i><gray>The average catch time is <b>%s</b>.", getAverageCatchTime())
        ));

        if (Settings.DEBUG) {
            lore.addAll(List.of(
                    Component.empty(),
                    TextUtils.miniText("<gray>ID: <white>%s", getId().toString()),
                    TextUtils.miniText("<gray>Bite Time: <white>%d - %d ticks", getBobberSettings().minBiteTime(), getBobberSettings().maxBiteTime()),
                    TextUtils.miniText("<gray>Lure Time: <white>%d - %d ticks", getBobberSettings().minLureTime(), getBobberSettings().maxLureTime()),
                    TextUtils.miniText("<gray>Idle Time: <white>%d - %d ticks", getBobberSettings().minIdleTime(), getBobberSettings().maxIdleTime())
            ));
        }

        return ItemStack.of(Material.FISHING_ROD).with(builder -> {
            builder.setTag(Tag.UUID("rod_id"), getId());
            builder.customName(getName());
            builder.set(DataComponents.LORE, lore);
        });
    }

    @Override
    public IBobber castBobber(Player player) {
        FishingBobber bobber = new FishingBobber(player, this);

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

    private @NotNull String getAverageCatchTime() {
        BobberSettings settings = getBobberSettings();

        double averageLureTicks = (settings.minLureTime() + settings.maxLureTime()) / 2.0;
        double averageIdleTicks = (settings.minIdleTime() + settings.maxIdleTime()) / 2.0;

        long averageCatchTime = (long) ((averageLureTicks + averageIdleTicks) * 50);
        return TextUtils.getFormattedTime(averageCatchTime, false, false);
    }
}
