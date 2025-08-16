package nl.devpieter.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class TextUtils {

    public static final Component CLEAR = Component.empty().style(Style.empty())
            .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
            .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
            .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
            .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

    public static @NotNull Component empty() {
        return CLEAR;
    }

    public static @NotNull Component miniText(String minimessageString) {
        return CLEAR.append(MiniMessage.miniMessage().deserialize(minimessageString));
    }

    public static @NotNull Component miniText(String minimessageString, Object... args) {
        return CLEAR.append(MiniMessage.miniMessage().deserialize(String.format(minimessageString, args)));
    }

    public static @NotNull String getFormattedTime(long timeMs, boolean includeMillis, boolean small) {
        long millis = timeMs % 1000;
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();
        if (minutes > 0) {
            if (small) formattedTime.append(minutes).append("m");
            else formattedTime.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
        }

        if (seconds > 0) {
            if (small) {
                if (!formattedTime.isEmpty()) formattedTime.append(" ");
                formattedTime.append(seconds).append("s");
            } else {
                if (!formattedTime.isEmpty()) formattedTime.append(" and ");
                formattedTime.append(seconds).append(" second").append(seconds != 1 ? "s" : "");
            }
        }

        if (includeMillis && millis > 0) {
            if (small) {
                if (!formattedTime.isEmpty()) formattedTime.append(" ");
                formattedTime.append(millis).append("ms");
            } else {
                if (!formattedTime.isEmpty()) formattedTime.append(" and ");
                formattedTime.append(millis).append(" millisecond").append(millis != 1 ? "s" : "");
            }
        }

        if (formattedTime.isEmpty()) {
            if (small) formattedTime.append("0 ms");
            else formattedTime.append("0 second").append(includeMillis ? "s" : "");
        }
        return formattedTime.toString();
    }
}
