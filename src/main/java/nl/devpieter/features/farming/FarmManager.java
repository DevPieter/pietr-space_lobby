package nl.devpieter.features.farming;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.farming.crops.berryBush.BerryBushPlaceable;
import nl.devpieter.features.farming.crops.genericCrop.GenericCropPlaceable;
import nl.devpieter.features.farming.crops.sugarCane.SugarCanePlaceable;
import nl.devpieter.features.signs.SignDecoder;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.features.signs.interfaces.ISignPlaceable;
import nl.devpieter.features.signs.models.SignHolder;
import nl.devpieter.models.BlockChangeHolder;
import nl.devpieter.models.BlockHolder;
import nl.devpieter.models.PointHolder;
import nl.devpieter.utils.minestom.TextUtils;
import nl.devpieter.utils.minestom.WorldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FarmManager {

    private static FarmManager INSTANCE;
    private static final String SIGN_KEY = "<FARM>";

    private final ComponentLogger logger = MinecraftServer.LOGGER;
    private final SignManager signManager = SignManager.getInstance();

    private final List<Entity> debugMarkers = new ArrayList<>();
    private final List<ISignPlaceable> placeables = new ArrayList<>();

    private final List<PointHolder> farmLocations = new ArrayList<>();
    private final List<BlockChangeHolder> placedBlocks = new ArrayList<>();

    private FarmManager() {
        placeables.addAll(List.of(
                new BerryBushPlaceable(),
                new GenericCropPlaceable(),
                new SugarCanePlaceable()
        ));
    }

    public static FarmManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FarmManager();
        return INSTANCE;
    }

    public void init() {
        revertFarms();
        scanAndPlaceFarms();

        if (EnvStatics.DEBUG) placeDebugMarkers();
    }

    private void scanAndPlaceFarms() {
        List<SignHolder> signs = signManager.getMatchedSigns(SIGN_KEY);
        for (SignHolder sign : signs) {

            ISignPlaceable placeable = getFreshFromSign(sign);
            if (placeable == null) continue;

            List<String> lines = sign.lines().subList(2, sign.lines().size());
            SignDecoder.decodeLines(lines, placeable.getDecoders());

            placedBlocks.addAll(placeable.place(
                    sign.block().instance(),
                    sign.block().position(),
                    sign
            ));

            farmLocations.add(PointHolder.of(sign.block()));
        }
    }

    public void revertFarms() {
        clearDebugMarkers();

        if (placedBlocks.isEmpty()) return;
        logger.info("Reverting {} farms to their original state.", placedBlocks.size());

        for (BlockChangeHolder changeHolder : placedBlocks) {
            BlockHolder original = changeHolder.original();
            changeHolder.instance().setBlock(original.position(), original.block());
        }

        placedBlocks.clear();
        farmLocations.clear();
    }

    public void placeDebugMarkers() {
        clearDebugMarkers();

        for (PointHolder point : farmLocations) {
            debugMarkers.add(WorldUtils.placeMarker(
                    point.instance(),
                    point.position().add(0.5, 0.7, 0.5),
                    TextUtils.miniText("<#ffcf4b>Farmable")
            ));
        }
    }

    public void clearDebugMarkers() {
        for (Entity marker : debugMarkers) marker.remove();
        debugMarkers.clear();
    }

    private @Nullable ISignPlaceable getFreshFromSign(@NotNull SignHolder sign) {
        try {
            String secondLine = sign.lines().get(1);

            for (ISignPlaceable signPlaceable : placeables) {
                if (!signPlaceable.getIdentifier().matches(secondLine)) continue;
                return (ISignPlaceable) signPlaceable.freshInstance();
            }
        } catch (Exception e) {
            Point pos = sign.block().position();
            logger.error("Error while getting farm placeable for sign at {}:{}", pos.x(), pos.y(), e);
        }

        return null;
    }
}
