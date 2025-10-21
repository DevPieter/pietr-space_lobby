package nl.devpieter.blockHandler;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class SignBlockHandler implements BlockHandler {

    @Override
    public @NotNull Key getKey() {
        return Key.key("minecraft", "sign");
    }
}
