package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import nl.devpieter.features.creatures.CreatureManager;

public class CreatureCommand extends Command {

    private final CreatureManager creatureManager = CreatureManager.getInstance();

    public CreatureCommand() {
        super("creature");

        Command init = new Command("init");
        init.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            player.sendMessage("Initializing, this may take a while...");
            creatureManager.init();
            player.sendMessage("Initialization complete.");
        });

        Command revert = new Command("revert");
        revert.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            creatureManager.revertCreatures();
            player.sendMessage("All creatures reverted to original state.");
        });

        Command debug = new Command("debug");
        debug.setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /creature debug <place|clear>");
        });

        Command debugPlace = new Command("place");
        debugPlace.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            creatureManager.placeDebugMarkers();
            player.sendMessage("Debug markers placed.");
        });

        Command debugClear = new Command("clear");
        debugClear.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            creatureManager.clearDebugMarkers();
            player.sendMessage("Debug markers cleared.");
        });

        debug.addSubcommand(debugPlace);
        debug.addSubcommand(debugClear);

        addSubcommand(init);
        addSubcommand(revert);
        addSubcommand(debug);
    }
}
