package space.pietr.lobby.common.entities;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.player.ResolvableProfile;
import space.pietr.lobby.common.utils.minestom.TextUtils;

public class Mannequin extends EntityCreature {

    public Mannequin(Player player) {
        super(EntityType.MANNEQUIN);

        editEntityMeta(MannequinMeta.class, meta -> {
            ResolvableProfile profile = new ResolvableProfile(player.getSkin());
            meta.setProfile(profile);

            meta.setCustomNameVisible(true);
            meta.setHasNoGravity(true);
            meta.setDescription(TextUtils.miniText("Parkour Replay"));
        });

        set(DataComponents.CUSTOM_NAME, player.getName());
    }
}
