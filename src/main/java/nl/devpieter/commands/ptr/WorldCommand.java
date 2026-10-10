package nl.devpieter.commands.ptr;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import nl.devpieter.features.world.WorldManager;
import space.pietr.lobby.common.features.world.LoadedWorld;
import space.pietr.lobby.common.features.world.settings.WorldSettings;
import space.pietr.lobby.common.utils.minestom.PlayerUtils;
import space.pietr.lobby.common.utils.minestom.TextUtils;

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

        addSubcommand(init);
        addSubcommand(getJoinWorldCommand());
        addSubcommand(getReloadWorldCommand());
        addSubcommand(getCreateWorldFolderCommand());
    }

    private ArgumentString getWorldIdArgument() {
        ArgumentString idArgument = ArgumentType.String("id");

        idArgument.setSuggestionCallback((_, _, suggestion) -> {
            for (LoadedWorld world : worldManager.getLoadedWorlds()) {

                Component separator = TextUtils.miniText("<gray> | ");

                Component info = TextUtils.combine(
                        TextUtils.miniText("<gray>ID: <white>%s", world.id()),
                        separator,
                        TextUtils.miniText("<gray>Name: <white>%s", world.name()),
                        separator,
                        TextUtils.miniText("<gray>Type: <white>%s", world.type().name())
                );

                suggestion.addEntry(new SuggestionEntry(world.id(), info));
            }
        });

        return idArgument;
    }

    private Command getJoinWorldCommand() {
        Command joinWorld = new Command("join");
        ArgumentString idArgument = getWorldIdArgument();

        joinWorld.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String id = context.get(idArgument);

            LoadedWorld world = worldManager.getLoadedWorldById(id);
            if (world == null) {
                player.sendMessage("World with ID: " + id + " not found.");
                return;
            }

            player.sendMessage("Joining world with ID: " + id);
            PlayerUtils.moveWorld(player, world);
        }, idArgument);

        return joinWorld;
    }

    private Command getReloadWorldCommand() {
        Command reloadWorld = new Command("reload");
        ArgumentString idArgument = getWorldIdArgument();

        reloadWorld.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String id = context.get(idArgument);
            boolean success = worldManager.reloadLoadedWorldById(id);

            if (success) player.sendMessage("World with ID: " + id + " reloaded.");
            else player.sendMessage("World with ID: " + id + " not found.");
        }, idArgument);

        return reloadWorld;
    }

    private Command getCreateWorldFolderCommand() {
        Command createFolder = new Command("create_world_folder");

        ArgumentString idArgument = ArgumentType.String("id");
        ArgumentString nameArgument = ArgumentType.String("name");

        createFolder.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            // TODO - Validate ID and Name
            String id = context.get(idArgument);
            String name = context.get(nameArgument);

            player.sendMessage("Creating world folder with ID: " + id + " and Name: " + name);
            worldManager.createWorldFolder(id, name, new WorldSettings());
            player.sendMessage("World folder created.");
        }, idArgument, nameArgument);

        return createFolder;
    }
}
