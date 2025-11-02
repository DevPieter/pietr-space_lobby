package nl.devpieter.features.economy.skills;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Player;
import nl.devpieter.features.economy.skills.categories.FarmingExperienceCategory;
import nl.devpieter.features.economy.skills.categories.FishingExperienceCategory;
import nl.devpieter.features.economy.skills.categories.TestExperienceCategory;
import nl.devpieter.features.economy.skills.category.IExperienceCategory;
import nl.devpieter.features.economy.skills.models.ExperienceHolder;
import nl.devpieter.helpers.BossBarHelper;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SkillManager {

    private static SkillManager INSTANCE;
    private final BossBarHelper bossBarHelper = BossBarHelper.getInstance();

    private final List<IExperienceCategory> categories = new ArrayList<>();
    private final HashMap<UUID, List<ExperienceHolder>> experienceHolders = new HashMap<>();

    private SkillManager() {
        categories.addAll(List.of(
                new TestExperienceCategory(),
                new FarmingExperienceCategory(),
                new FishingExperienceCategory()
        ));
    }

    public static SkillManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SkillManager();
        return INSTANCE;
    }

    public @Nullable IExperienceCategory getCategory(String id) {
        for (IExperienceCategory category : categories) {
            if (!category.getId().equals(id)) continue;
            return category;
        }

        return null;
    }

    public void grantExperience(Player player, String categoryId, double amount) {
        grantExperience(player, categoryId, amount, true);
    }

    public void grantExperience(Player player, String categoryId, double amount, double bonus) {
        grantExperience(player, categoryId, amount, bonus, true);
    }

    public void grantExperience(Player player, String categoryId, double amount, boolean notify) {
        grantExperience(player, categoryId, amount, 0, notify);
    }

    public void grantExperience(Player player, String categoryId, double amount, double bonus, boolean notify) {
        if (amount <= 0) return;
        if (bonus < 0) bonus = 0;

        ExperienceHolder experienceHolder = getExperience(player.getUuid(), categoryId);
        experienceHolder.grantExperience(amount + bonus);

        if (!notify) return;

        IExperienceCategory category = getCategory(categoryId);
        if (category == null) return;

        notifyPlayerGrant(player, category, experienceHolder, amount, bonus);
    }

    public void revokeExperience(Player player, String categoryId, double amount) {
        revokeExperience(player, categoryId, amount, true, null);
    }

    public void revokeExperience(Player player, String categoryId, double amount, @Nullable String reason) {
        revokeExperience(player, categoryId, amount, true, reason);
    }

    public void revokeExperience(Player player, String categoryId, double amount, boolean notify, @Nullable String reason) {
        if (amount <= 0) return;

        ExperienceHolder experienceHolder = getExperience(player.getUuid(), categoryId);
        experienceHolder.revokeExperience(amount);

        if (!notify) return;

        IExperienceCategory category = getCategory(categoryId);
        if (category == null) return;

        notifyPlayerRevoke(player, category, amount, reason);
    }

    private @NotNull ExperienceHolder getExperience(UUID uuid, String balanceId) {
        experienceHolders.putIfAbsent(uuid, new ArrayList<>());
        List<ExperienceHolder> holders = experienceHolders.get(uuid);

        for (ExperienceHolder holder : holders) {
            if (!holder.categoryId().equals(balanceId)) continue;
            return holder;
        }

        ExperienceHolder newHolder = new ExperienceHolder(uuid, balanceId);
        holders.add(newHolder);

        return newHolder;
    }

    private void notifyPlayerGrant(Player player, IExperienceCategory category, ExperienceHolder experience, double amount, double bonus) {
        experience.updateCache();

        Color plusColor = category.getColor();
        String plusHex = TextColor.color(plusColor.getRGB()).asHexString();

        Component message = TextUtils.combine(
                TextUtils.miniText("<#576272><%s>+</%s>%s", plusHex, plusHex, TextUtils.getFormattedDouble(amount)),
                Component.space(),
                category.getName()
        );

        if (bonus > 0) message = TextUtils.combine(
                message,
                Component.space(),
                TextUtils.miniText("<#576272>and"),
                Component.space(),
                TextUtils.miniText("<#576272><#FFD700>+</#FFD700>%s", TextUtils.getFormattedDouble(bonus)),
                Component.space(),
                TextUtils.miniText("<gradient:#FFD700:#FFA500>Bonus</gradient>")
        );

        message = TextUtils.combine(
                message,
                Component.space(),
                TextUtils.miniText("<#576272>XP!")
        );

        player.sendActionBar(message);
        showBossBar(player, category, experience);
    }

    private void notifyPlayerRevoke(Player player, IExperienceCategory category, double amount, @Nullable String reason) {
        Component message = TextUtils.combine(
                TextUtils.miniText("<#eb7b65>-<#576272>%s", TextUtils.getFormattedDouble(amount)),
                Component.space(),
                category.getName(),
                Component.space(),
                TextUtils.miniText("<#576272>XP.")
        );

        player.sendActionBar(message);

        if (reason != null) player.sendMessage(TextUtils.combine(
                TextUtils.miniText("<#576272>%s", reason),
                Component.space(),
                TextUtils.miniText("<#eb7b65>-<#576272>%s", TextUtils.getFormattedDouble(amount)),
                Component.space(),
                category.getName(),
                Component.space(), TextUtils.miniText("<#576272>XP!")
        ));
    }

    private void showBossBar(Player player, IExperienceCategory category, ExperienceHolder experience) {
        Color plusColor = category.getColor();
        String plusHex = TextColor.color(plusColor.getRGB()).asHexString();

        Component text = TextUtils.combine(
                category.getName(),
                Component.space(),
                TextUtils.miniText("<%s>Level", plusHex),
                Component.space(),
                TextUtils.miniText("<%s><b>%d", plusHex, experience.cachedLevel())
        );

        if (experience.cachedPrestige() > 0) {
            text = TextUtils.combine(
                    text,
                    Component.space(),
                    TextUtils.miniText("<%s>Prestige", plusHex),
                    Component.space(),
                    TextUtils.miniText("<%s><b>%d", plusHex, experience.cachedPrestige())
            );
        }

        BossBar bossBar = BossBar.bossBar(
                text,
                (float) (experience.cachedProgressPercentage() / 100f),
                category.getBossBarColor(),
                BossBar.Overlay.PROGRESS
        );

        bossBarHelper.showTempBossBar(
                player,
                "skill_experience",
                bossBar,
                Duration.ofSeconds(5)
        );
    }
}
