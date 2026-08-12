package nl.devpieter.listeners;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.minestom.server.scoreboard.TeamManager;
import nl.devpieter.features.scoreboard.ScoreboardManager;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

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

        ScoreboardManager scoreboardManager = ScoreboardManager.getInstance();
        scoreboardManager.test(player);

        player.setInvisible(false);
        player.setAutoViewable(true);

        return Result.SUCCESS;
    }
}
