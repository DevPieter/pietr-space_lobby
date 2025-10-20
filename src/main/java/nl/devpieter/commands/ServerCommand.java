package nl.devpieter.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;

import java.util.UUID;

public class ServerCommand extends Command {

    public ServerCommand() {
        super("connect");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /connect <server>");
        });

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        ArgumentString serverArgument = ArgumentType.String("server");

        addSyntax((sender, context) -> {
            String server = context.get(serverArgument);
            if (!(sender instanceof Player player)) return;

            player.sendPluginMessage("bungeecord:main", NetworkBuffer.makeArray(buffer -> {
                buffer.write(NetworkBuffer.STRING_IO_UTF8, "Connect");
                buffer.write(NetworkBuffer.STRING_IO_UTF8, server);
            }));
        }, serverArgument);
    }
}
