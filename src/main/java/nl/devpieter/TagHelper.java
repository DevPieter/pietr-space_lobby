package nl.devpieter;

import nl.devpieter.entities.LobbyPlayer;
import nl.devpieter.enums.PlayerTags;
import nl.devpieter.features.tips.MsgManager;

public class TagHelper {

    private static final MsgManager msgManager = MsgManager.getInstance();

    public static boolean canSitPlayer(LobbyPlayer player) {
        if (player.hasTag(PlayerTags.IN_PARKOUR)) {
            msgManager.showMessage(player, "cannot_sit_in_parkour", "<red>You cannot sit while doing a parkour!");
            return false;
        }

        if (player.hasTag(PlayerTags.IN_PARKOUR_REPLAY)) {
            msgManager.showMessage(player, "cannot_sit_in_replay", "<red>You cannot sit while watching a replay!");
            return false;
        }

        if (player.hasTag(PlayerTags.SITTING)) {
            msgManager.showMessage(player, "cannot_sit_while_sitting", "<red>You are already sitting!");
            return false;
        }

        if (player.hasTag(PlayerTags.LAYING)) {
            msgManager.showMessage(player, "cannot_sit_while_laying", "<red>You cannot sit while laying down!");
            return false;
        }

        return true;
    }

    public static boolean canLayPlayer(LobbyPlayer player) {
        if (player.hasTag(PlayerTags.IN_PARKOUR)) {
            msgManager.showMessage(player, "cannot_lay_in_parkour", "<red>You cannot lay down while doing a parkour!");
            return false;
        }

        if (player.hasTag(PlayerTags.IN_PARKOUR_REPLAY)) {
            msgManager.showMessage(player, "cannot_lay_in_replay", "<red>You cannot lay down while watching a replay!");
            return false;
        }

        if (player.hasTag(PlayerTags.LAYING)) {
            msgManager.showMessage(player, "cannot_lay_while_laying", "<red>You are already laying down!");
            return false;
        }

        if (player.hasTag(PlayerTags.SITTING)) {
            msgManager.showMessage(player, "cannot_lay_while_sitting", "<red>You cannot lay down while sitting!");
            return false;
        }

        return true;
    }

    public static boolean canStartParkour(LobbyPlayer player) {
        if (player.hasTag(PlayerTags.IN_PARKOUR)) {
            msgManager.showMessage(player, "cannot_start_parkour_in_parkour", "<red>You cannot start a parkour while already in a parkour!");
            return false;
        }

        if (player.hasTag(PlayerTags.IN_PARKOUR_REPLAY)) {
            msgManager.showMessage(player, "cannot_start_parkour_in_replay", "<red>You cannot start a parkour while watching a replay!");
            return false;
        }

        if (player.hasTag(PlayerTags.SITTING)) {
            msgManager.showMessage(player, "cannot_start_parkour_while_sitting", "<red>You cannot start a parkour while sitting!");
            return false;
        }

        if (player.hasTag(PlayerTags.LAYING)) {
            msgManager.showMessage(player, "cannot_start_parkour_while_laying", "<red>You cannot start a parkour while laying down!");
            return false;
        }

        return true;
    }

    public static boolean canStartReplay(LobbyPlayer player) {
        if (player.hasTag(PlayerTags.IN_PARKOUR_REPLAY)) {
            msgManager.showMessage(player, "cannot_start_replay_in_replay", "<red>You are already watching a replay!");
            return false;
        }

        if (player.hasTag(PlayerTags.IN_PARKOUR)) {
            msgManager.showMessage(player, "cannot_start_replay_in_parkour", "<red>You cannot start a replay while doing a parkour!");
            return false;
        }

        if (player.hasTag(PlayerTags.SITTING)) {
            msgManager.showMessage(player, "cannot_start_replay_while_sitting", "<red>You cannot start a replay while sitting!");
            return false;
        }

        if (player.hasTag(PlayerTags.LAYING)) {
            msgManager.showMessage(player, "cannot_start_replay_while_laying", "<red>You cannot start a replay while laying down!");
            return false;
        }

        return true;
    }
}
