package nl.devpieter.commands.world;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import nl.devpieter.features.portals.PortalManager;

import java.util.UUID;

public class PortalCommand extends Command {

    private final PortalManager portalManager = PortalManager.getInstance();

    public PortalCommand() {
        super("portal");

        setCondition((sender, commandString) -> {
            if (!(sender instanceof Player player)) return false;
            return player.getUuid().equals(UUID.fromString("c292f4e0-f990-4592-a067-f5f77c15d408"));
        });

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /portal <init|revert|debug>");
        });

        Command init = new Command("init");
        init.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            player.sendMessage("Initializing, this may take a while...");
            portalManager.init();
            player.sendMessage("Initialization complete.");
        });

        Command revert = new Command("revert");
        revert.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            portalManager.revertPortals();
            player.sendMessage("All portals reverted to original state.");
        });

        Command debug = new Command("debug");
        debug.setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /portal debug <place|clear>");
        });

        Command debugPlace = new Command("place");
        debugPlace.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            portalManager.placeDebugMarkers();
            player.sendMessage("Debug markers placed.");
        });

        Command debugClear = new Command("clear");
        debugClear.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            portalManager.clearDebugMarkers();
            player.sendMessage("Debug markers cleared.");
        });

        debug.addSubcommand(debugPlace);
        debug.addSubcommand(debugClear);

        addSubcommand(init);
        addSubcommand(revert);
        addSubcommand(debug);
    }
}
