package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.features.world.models.settings.WorldSettings;

public class WorldCommand extends Command {

    private final WorldManager worldManager = WorldManager.getInstance();

    public WorldCommand() {
        super("world");

        Command init = new Command("init");
        init.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            player.sendMessage("Initializing worlds, this may take a while...");
            worldManager.init();
            player.sendMessage("Worlds initialized.");
        });

        Command createFolder = new Command("create_world_folder");
        ArgumentString idArgument = ArgumentType.String("id");
        ArgumentString nameArgument = ArgumentType.String("name");
        createFolder.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String id = context.get(idArgument);
            String name = context.get(nameArgument);
            player.sendMessage("Creating world folder with ID: " + id + " and Name: " + name);
            worldManager.createWorldFolder(id, name, new WorldSettings());
            player.sendMessage("World folder created.");
        }, idArgument, nameArgument);

        addSubcommand(init);
        addSubcommand(createFolder);
    }
}
