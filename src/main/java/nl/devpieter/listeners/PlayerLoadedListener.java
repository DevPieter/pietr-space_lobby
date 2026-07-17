package nl.devpieter.listeners;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.*;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.timer.TaskSchedule;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class PlayerLoadedListener implements EventListener<@NotNull PlayerLoadedEvent> {

    private final TeamManager teamManager = MinecraftServer.getTeamManager();

    private final Component welcome = TextUtils.miniText(
            "<gray>Welcome to <b><gradient:#6A75AF:#303449>Pietr 🚀 Space</gradient></b>!\n" +
                    "<gray>Use <yellow>/server</yellow> to switch between worlds."
    );

    @Override
    public @NotNull Class<PlayerLoadedEvent> eventType() {
        return PlayerLoadedEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull PlayerLoadedEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(this.welcome);

        AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        attribute.setBaseValue(0);
        attribute.clearModifiers();

        Team team = teamManager.getTeam("pietr");
        if (team == null) team = new TeamBuilder("pietr", teamManager).build();
        team.updateCollisionRule(TeamsPacket.CollisionRule.NEVER);
        player.setTeam(team);

        player.setInvisible(false);
        player.setAutoViewable(true);

        // AdvancementTabEvent
//        Advancement

//        AdvancementTab tab = MinecraftServer.getAdvancementManager().getTab("minecraft:adventure");

        AdvancementTab tab = init(player.getUuid());
        tab.addViewer(player);

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            grandWanderer(player);
        }, TaskSchedule.seconds(5), TaskSchedule.stop());

        return Result.SUCCESS;
    }

    private static HashMap<String, Advancement> advancements = new HashMap<>();

    private AdvancementTab init(UUID uuid) {
        AdvancementManager manager = MinecraftServer.getAdvancementManager();

        AdvancementTab existing = manager.getTab("pietr:lobby_" + uuid);
        if (existing != null) return existing;

        AdvancementRoot root = new AdvancementRoot(
                TextUtils.miniText("Lobby Exploration"),
                TextUtils.miniText("Explore the Lobby of <gradient:#6A75AF:#303449>Pietr 🚀 Space</gradient>"),
                Material.COMPASS,
                FrameType.TASK,
                0, 0,
                null
        );

        AdvancementTab tab = manager.createTab("pietr:lobby_" + uuid, root);

        Advancement wanderer = new Advancement(
                TextUtils.miniText("Wanderer"),
                TextUtils.miniText("Walk around the lobby."),
                ItemStack.of(Material.LEATHER_BOOTS),
                FrameType.TASK,
                1, 0,
                false
        );

        wanderer.showToast(true);
        advancements.put("wanderer_" + uuid, wanderer);

        tab.createAdvancement("pietr:wanderer", wanderer, root);
        return tab;
    }

    public static void grandWanderer(Player player) {
        Advancement wanderer = advancements.get("wanderer_" + player.getUuid());
        if (wanderer == null) return;

        wanderer.setAchieved(true);
    }
}
