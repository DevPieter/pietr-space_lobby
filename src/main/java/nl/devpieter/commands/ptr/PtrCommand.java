package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class PtrCommand extends Command {

    public PtrCommand() {
        super("ptr");

        setCondition((sender, _) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        setDefaultExecutor((sender, _) -> sender.sendMessage("Meow :3"));

        addSubcommands(
                new WorldCommand(),
                new SignCommand(),
                new CreatureCommand(),
                new FarmCommand(),
                new ParkourCommand(),
                new PortalCommand()
        );
    }
}
