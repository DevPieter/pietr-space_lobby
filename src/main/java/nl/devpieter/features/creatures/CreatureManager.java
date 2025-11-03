package nl.devpieter.features.creatures;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.creatures.entities.cuteCat.CuteCatSpawnable;
import nl.devpieter.features.creatures.entities.cuteFox.CuteFoxSpawnable;
import nl.devpieter.features.creatures.models.CreaturePointHolder;
import nl.devpieter.features.signs.SignDecoder;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.interfaces.ISignSpawnable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.minestom.TextUtils;
import nl.devpieter.utils.minestom.WorldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CreatureManager {

    private static CreatureManager INSTANCE;
    private static final String SIGN_KEY = "<ENTITY>";

    private final ComponentLogger logger = MinecraftServer.LOGGER;
    private final SignManager signManager = SignManager.getInstance();

    private final List<Entity> debugMarkers = new ArrayList<>();
    private final List<ISignSpawnable> spawnables = new ArrayList<>();

    private final List<CreaturePointHolder> creatureLocations = new ArrayList<>();
    private final List<Entity> loadedCreatures = new ArrayList<>();

    private CreatureManager() {
        spawnables.addAll(List.of(
                new CuteCatSpawnable(),
                new CuteFoxSpawnable()
        ));
    }

    public static CreatureManager getInstance() {
        if (INSTANCE == null) INSTANCE = new CreatureManager();
        return INSTANCE;
    }

    public void init() {
        revertCreatures();
        scanAndPlaceCreatures();

        if (EnvStatics.DEBUG) placeDebugMarkers();
    }

    private void scanAndPlaceCreatures() {
        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {

            ISignSpawnable spawnable = getFreshFromSign(sign);
            if (spawnable == null) continue;

            List<String> lines = sign.lines().subList(2, sign.lines().size());
            SignDecoder.decodeLines(lines, spawnable.getDecoders());

            Entity entity = spawnable.spawn(sign.block().instance(), sign.block().position(), sign);
            loadedCreatures.add(entity);

            creatureLocations.add(new CreaturePointHolder(
                    spawnable.getIdentifier(), PointHolder.of(sign.block())
            ));
        }
    }

    public void revertCreatures() {
        clearDebugMarkers();

        if (loadedCreatures.isEmpty()) return;
        logger.info("Reverting {} creatures to their original state.", loadedCreatures.size());

        for (Entity entity : loadedCreatures) {
            entity.remove();
        }

        loadedCreatures.clear();
        creatureLocations.clear();
    }

    public void placeDebugMarkers() {
        clearDebugMarkers();

        for (CreaturePointHolder holder : creatureLocations) {
            debugMarkers.add(WorldUtils.placeMarker(
                    holder.point().instance(),
                    holder.point().position().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#ff4b8a>Creature <gray>%s", holder.identifier())
            ));
        }
    }

    public void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }

    private @Nullable ISignSpawnable getFreshFromSign(@NotNull SignHolder sign) {
        try {
            String secondLine = sign.lines().get(1);

            for (ISignSpawnable signSpawnable : spawnables) {
                if (!signSpawnable.getIdentifier().matches(secondLine)) continue;
                return (ISignSpawnable) signSpawnable.freshInstance();
            }
        } catch (Exception e) {
            Point pos = sign.block().position();
            logger.error("Error while getting creature spawnable for sign at {}:{}", pos.x(), pos.y(), e);
        }

        return null;
    }
}
