package nl.devpieter.features.mining.tool;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import nl.devpieter.utils.minestom.TextUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Tool implements ITool {

    protected abstract ItemStack getBaseItem();

    @Override
    public ItemStack buildItem() {
        List<Component> lore = new ArrayList<>(List.of(
                TextUtils.miniText("<i><gray>Mining Strength: <b>%s</b>", TextUtils.getFormattedDouble(getMiningStrength())),
                TextUtils.miniText("<i><gray>Mining Speed Multiplier: <b>%sx</b>", TextUtils.getFormattedDouble(getMiningSpeedMultiplier()))
        ));

        return getBaseItem().with(builder -> {
            builder.setTag(Tag.UUID("tool_id"), getId());
            builder.customName(getName());
            builder.set(DataComponents.LORE, lore);
        });
    }
}
