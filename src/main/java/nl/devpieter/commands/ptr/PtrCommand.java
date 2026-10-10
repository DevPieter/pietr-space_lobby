package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import nl.devpieter.sees.Sees;
import space.pietr.lobby.api.event.command.PtrCommandInitializingEvent;

import java.util.UUID;

public class PtrCommand extends Command {

    public PtrCommand() {
        super("ptr");

        setCondition((sender, _) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        setDefaultExecutor((sender, _) -> sender.sendMessage("Meow :3"));

        var sees = Sees.getSharedInstance();
        var events = sees.dispatchWithResult(new PtrCommandInitializingEvent());
        for (Command subCommand : events) addSubcommand(subCommand);

        addSubcommands(
                new WorldCommand(),
                new SignCommand(),
                new ParkourCommand(),
                new PortalCommand(),
                new PluginCommand()
        );

    }
}
