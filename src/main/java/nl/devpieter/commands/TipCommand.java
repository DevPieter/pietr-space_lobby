package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import nl.devpieter.features.tips.TipManager;
import space.pietr.lobby.common.utils.minestom.TextUtils;

public class TipCommand extends Command {

    private final TipManager tipManager = TipManager.getInstance();

    public TipCommand() {
        super("tip");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /tip <enable|disable|ignore> [tipId]");
        });

        Command enable = new Command("enable");
        enable.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            tipManager.disableTips(player, false);
            player.sendMessage(TextUtils.miniText("<#95ed9c>Tips have been <b>enabled</b>."));
        });

        Command disable = new Command("disable");
        disable.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            tipManager.disableTips(player, true);
            player.sendMessage(TextUtils.miniText("<#ed9595>Tips have been <b>disabled</b>."));
        });

        Command ignore = new Command("ignore");
        ArgumentString tipIdArg = ArgumentType.String("tipId");
        ArgumentBoolean silentArg = ArgumentType.Boolean("silent");

        ignore.addSyntax(ignoreExecutor(tipIdArg, silentArg), tipIdArg);
        ignore.addSyntax(ignoreExecutor(tipIdArg, silentArg), tipIdArg, silentArg);

        addSubcommand(enable);
        addSubcommand(disable);
        addSubcommand(ignore);
    }

    private CommandExecutor ignoreExecutor(ArgumentString tipIdArg, ArgumentBoolean silentArg) {
        return (sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String tipId = context.get(tipIdArg);
            boolean silent = context.has(silentArg) && context.get(silentArg);

            boolean success = tipManager.ignoreTip(player, tipId);
            if (silent && success) player.sendActionBar(TextUtils.miniText(
                    "<#dce37d>This tip will no longer be shown to you."
            ));

            if (silent) return;

            if (success) player.sendMessage(TextUtils.miniText(
                    "<#dce37d>The tip with ID <b>%s</b> has been <b>ignored</b>.",
                    tipId
            ));
            else player.sendMessage(TextUtils.miniText(
                    "<#ed9595>You have not received the tip with ID <b>%s</b>, so it cannot be ignored.",
                    tipId
            ));
        };
    }
}
