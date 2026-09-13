package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import nl.devpieter.features.portals.PortalManager;

public class PortalCommand extends Command {

    private final PortalManager portalManager = PortalManager.getInstance();

    public PortalCommand() {
        super("portal");

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
