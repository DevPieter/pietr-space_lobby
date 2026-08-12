package nl.devpieter.features.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import nl.devpieter.utils.minestom.TextUtils;

public class ScoreboardManager {

    private static ScoreboardManager INSTANCE;

    private ScoreboardManager() {
    }

    public static ScoreboardManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ScoreboardManager();
        return INSTANCE;
    }

    public void test(Player player) {
        Sidebar scoreboard = new Sidebar(Component.text("Test Scoreboard"));
        scoreboard.setTitle(TextUtils.miniText("  <b><gradient:#6A75AF:#303449>Pietr 🚀 Space</gradient></b>  "));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "blank30",
                Component.empty(),
                30,
                Sidebar.NumberFormat.blank()
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "online",
                TextUtils.miniText("<#6A75AF>\uD83D\uDFE2 <white>Online"),
                29,
                Sidebar.NumberFormat.fixed(TextUtils.miniText("<#6A75AF>3 "))
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "keyall",
                TextUtils.miniText("<#af6a6a>\uD83D\uDD12 <white>Keyall"),
                28,
                Sidebar.NumberFormat.fixed(TextUtils.miniText("<#af6a6a>Soon™ "))
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "blank20",
                Component.empty(),
                20,
                Sidebar.NumberFormat.blank()
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "money",
                TextUtils.miniText("<#32a883>\uD83D\uDCB0 <white>Money"),
                19,
                Sidebar.NumberFormat.fixed(TextUtils.miniText("<#32a883>16,5K "))
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "shards",
                TextUtils.miniText("<#776aaf>⭐ <white>Shards"),
                18,
                Sidebar.NumberFormat.fixed(TextUtils.miniText("<#776aaf>1,2K "))
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "keys",
                TextUtils.miniText("<#af6a9f>\uD83D\uDD11 <white>Keys"),
                17,
                Sidebar.NumberFormat.fixed(TextUtils.miniText("<#af6a9f>7 "))
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "blank10",
                Component.empty(),
                10,
                Sidebar.NumberFormat.blank()
        ));

        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "footer",
                TextUtils.miniText("   <gray>play.pietr.space"),
                0,
                Sidebar.NumberFormat.blank()
        ));

        scoreboard.addViewer(player);
    }
}

