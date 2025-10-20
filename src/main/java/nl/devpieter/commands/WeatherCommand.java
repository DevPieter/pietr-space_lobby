package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Weather;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WeatherCommand extends Command {

    public WeatherCommand() {
        super("weather");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /weather <mode>");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        ArgumentEnum<@NotNull WeatherMode> weatherArgument = ArgumentType.Enum("weather", WeatherMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            WeatherMode weatherMode = context.get(weatherArgument);
            if (!(sender instanceof Player player)) return;

            Weather weather;
            switch (weatherMode) {
                case RAIN -> weather = Weather.RAIN;
                case THUNDER -> weather = Weather.THUNDER;
                default -> weather = Weather.CLEAR;
            }

            player.getInstance().setWeather(weather);
            player.sendMessage("The weather has been set to " + weatherMode.name());
        }, weatherArgument);

    }

    enum WeatherMode {
        CLEAR,
        RAIN,
        THUNDER
    }
}
