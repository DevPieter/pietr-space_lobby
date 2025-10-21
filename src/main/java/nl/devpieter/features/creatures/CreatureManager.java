package nl.devpieter.features.creatures;

import net.minestom.server.entity.Entity;
import nl.devpieter.features.creatures.entities.cuteCat.CuteCatSpawnable;
import nl.devpieter.features.creatures.entities.cuteFox.CuteFoxSpawnable;
import nl.devpieter.features.signs.SignDecoder;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.features.signs.partDecoder.IPartDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreatureManager {

    private static CreatureManager INSTANCE;

    private static final String SIGN_KEY = "<ENTITY>";

    private final SignManager signManager = SignManager.getInstance();

    private final List<ISignSpawnable> signSpawnables = new ArrayList<>();
    private final List<Entity> loadedEntities = new ArrayList<>();

    private CreatureManager() {
        signSpawnables.addAll(List.of(
                new CuteCatSpawnable(),
                new CuteFoxSpawnable()
        ));
    }

    public static CreatureManager getInstance() {
        if (INSTANCE == null) INSTANCE = new CreatureManager();
        return INSTANCE;
    }

    public void init() {
        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {

            ISignSpawnable signEntity = getFromSign(sign);
            if (signEntity == null) continue;

            List<String> lines = sign.lines().subList(2, sign.lines().size());
            HashMap<Integer, List<IPartDecoder<?>>> decoders = signEntity.getDecoders();

            SignDecoder.decodeLines(lines, decoders);

            Entity entity = signEntity.spawn(sign.block().instance(), sign.block().position(), sign);
            loadedEntities.add(entity);
        }
    }

    private @Nullable ISignSpawnable getFromSign(@NotNull SignHolder sign) {
        String secondLine = sign.lines().get(1);

        for (ISignSpawnable signSpawnable : signSpawnables) {
            if (!signSpawnable.getIdentifier().matches(secondLine)) continue;
            return signSpawnable;
        }

        return null;
    }
}
