package nl.devpieter.commands.ptr;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import nl.devpieter.LobbyApiImpl;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.world.WorldManager;
import nl.devpieter.plugin.PluginManager;

import java.util.Optional;


public class PluginCommand extends Command {

    public PluginCommand() {
        super("plugin");

        Command init = new Command("init");
        init.setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            var pluginManager = PluginManager.getInstance();
            pluginManager.init();

            PluginManager.getInstance().apiAvailable(new LobbyApiImpl(
                    SignManager.getInstance(),
                    WorldManager.getInstance()
            ));
        });


        addSubcommand(init);
    }
}
