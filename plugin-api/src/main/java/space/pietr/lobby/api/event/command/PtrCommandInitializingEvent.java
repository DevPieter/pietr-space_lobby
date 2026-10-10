package space.pietr.lobby.api.event.command;

import net.minestom.server.command.builder.Command;
import nl.devpieter.sees.event.SReturnableEvent;

import java.util.ArrayList;
import java.util.List;

public class PtrCommandInitializingEvent implements SReturnableEvent<List<Command>> {

    private final List<Command> subCommands = new ArrayList<>();

    public PtrCommandInitializingEvent addSubCommand(Command... commands) {
        subCommands.addAll(List.of(commands));
        return this;
    }

    @Override
    public List<Command> getResult() {
        return subCommands;
    }

    @Override
    public void setResult(List<Command> result) {
        subCommands.clear();
        subCommands.addAll(result);
    }
}
