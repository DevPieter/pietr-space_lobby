package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import nl.devpieter.features.parkour.ParkourManager;

public class ParkourCommand extends Command {

    private final ParkourManager parkourManager = ParkourManager.getInstance();

    public ParkourCommand() {
        super("parkour");

        Command init = new Command("init");
        init.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            player.sendMessage("Initializing, this may take a while...");
            parkourManager.init();
            player.sendMessage("Initialization complete.");
        });

        Command revert = new Command("revert");
        revert.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            parkourManager.revertParkours();
            player.sendMessage("All parkours reverted to original state.");
        });

        Command debug = new Command("debug");
        debug.setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /parkour debug <place|clear>");
        });

        Command debugPlace = new Command("place");
        debugPlace.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            parkourManager.placeDebugMarkers();
            player.sendMessage("Debug markers placed.");
        });

        Command debugClear = new Command("clear");
        debugClear.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            parkourManager.clearDebugMarkers();
            player.sendMessage("Debug markers cleared.");
        });

        debug.addSubcommand(debugPlace);
        debug.addSubcommand(debugClear);

        addSubcommand(init);
        addSubcommand(revert);
        addSubcommand(debug);
    }
}
