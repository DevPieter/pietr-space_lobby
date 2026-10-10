package nl.devpieter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.entity.Player;
import nl.devpieter.config.EnvStatics;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import space.pietr.lobby.common.utils.minestom.TextUtils;

public class PlayLogger {

    private static final PlayLogger INSTANCE = new PlayLogger();

    private static final String DEBUG_PREFIX = "<green>[Debug]";
    private static final String INFO_PREFIX = "<blue>[Info]";

    private final ComponentLogger logger = ComponentLogger.logger(PlayLogger.class);

    private PlayLogger() {
    }

    public static PlayLogger getInstance() {
        return INSTANCE;
    }

    public void debug(String message, Object... args) {
        debug(null, false, message, args);
    }

    public void debug(@Nullable Player player, String message, Object... args) {
        debug(player, false, message, args);
    }

    public void debug(@Nullable Player player, boolean actionBar, String message, Object... args) {
        if (!EnvStatics.DEBUG) return;
        Component msg = buildMessage(TextUtils.miniText(DEBUG_PREFIX), message, args);

        if (player != null) {
            if (actionBar) player.sendActionBar(buildMessage(null, message, args));
            else player.sendMessage(msg);
        }

        logger.info(appendTarget(player, msg));
    }

    public void info(String message, Object... args) {
        info(null, false, message, args);
    }

    public void info(@Nullable Player player, String message, Object... args) {
        info(player, false, message, args);
    }

    public void info(@Nullable Player player, boolean actionBar, String message, Object... args) {
        Component msg = buildMessage(TextUtils.miniText(INFO_PREFIX), message, args);

        if (player != null && EnvStatics.DEBUG) {
            if (actionBar) player.sendActionBar(buildMessage(null, message, args));
            else player.sendMessage(msg);
        }

        logger.info(appendTarget(player, msg));
    }

    private Component buildMessage(@Nullable Component prefix, String message, Object... args) {
        if (prefix == null) return messagePart(message, args);

        return TextUtils.combine(
                prefix,
                Component.space(),
                messagePart(message, args)
        );
    }

    private Component appendTarget(@Nullable Player player, Component message) {
        if (player == null) return message;

        return TextUtils.combine(
                message,
                Component.space(),
                TextUtils.miniText("<gray>(T: " + player.getUsername() + ")</gray>")
        );
    }

    private Component messagePart(@NonNull String message, Object... args) {
        Component result = Component.empty();
        String[] parts = message.split("\\{}", -1);

        for (int i = 0; i < parts.length; i++) {

            result = result.append(TextUtils.miniText("<gray>" + parts[i] + "</gray>"));
            if (i >= args.length) break;

            Object arg = args[i];

            if (arg instanceof Component componentArg) result = result.append(componentArg);
            else result = result.append(TextUtils.miniText("<yellow>" + arg + "</yellow>"));
        }

        return result;
    }
}
