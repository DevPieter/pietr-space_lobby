package nl.devpieter.utils.minestom;

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

    public static @NotNull Component combine(@NotNull Component... components) {
        Component combined = CLEAR;
        for (Component component : components) combined = combined.append(component);

        return combined;
    }

    public static @NotNull String getFormattedDouble(double value) {
        if (value % 1 == 0) return String.format("%,.0f", value);
        else return String.format("%,.2f", value);
    }

    public static @NotNull String getFormattedTime(long timeMs, boolean includeMillis, boolean small) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        double seconds = (timeMs % 60000) / 1000.0;

        StringBuilder formattedTime = new StringBuilder();

        if (minutes > 0) {
            if (small) formattedTime.append(minutes).append("m ");
            else formattedTime.append(minutes).append(" minute").append(minutes != 1 ? "s" : "").append(" ");
        }

        if (includeMillis) {
            if (small) formattedTime.append(String.format("%.2fs", seconds));
            else formattedTime.append(String.format("%.2f seconds", seconds));
        } else {
            if (small) formattedTime.append((int) seconds).append("s");
            else formattedTime.append((int) seconds).append(" second").append(seconds != 1 ? "s" : "");
        }

        return formattedTime.toString().trim();
    }
}
