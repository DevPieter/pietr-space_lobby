package nl.devpieter.features.structures;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import nl.devpieter.Lobby;
import space.pietr.lobby.common.EnvStatics;
import nl.devpieter.features.structures.models.Structure;
import nl.devpieter.features.structures.models.nbt.StructureNbt;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructureManager {

    private static StructureManager INSTANCE;

    private static final int MAX_FOLDER_DEPTH = 10;
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_NAME_REGEX = "[a-z0-9_-]+";

    private final Logger logger = Lobby.getLogger(this.getClass());

    private final HashMap<String, List<Structure>> structures = new HashMap<>();

    private StructureManager() {
    }

    public static StructureManager getInstance() {
        if (INSTANCE == null) INSTANCE = new StructureManager();
        return INSTANCE;
    }

    public void init() {
        scanStructureFolder();
    }

    public @Nullable Structure getStructure(@Nullable String id) {
        return getStructure("", id);
    }

    public @Nullable Structure getStructure(@Nullable String path, @Nullable String id) {
        if (path == null || id == null) return null;

        List<Structure> structureList = structures.get(path);
        if (structureList == null) return null;

        for (Structure structure : structureList) {
            if (!structure.id().equals(id)) continue;
            return structure;
        }

        return null;
    }

    private void scanStructureFolder() {
        logger.info("Scanning structure folder in: {}", EnvStatics.STRUCTURE_DATA_PATH);
        File structuresDir = new File(EnvStatics.STRUCTURE_DATA_PATH);

        if (!structuresDir.exists() || !structuresDir.isDirectory()) {
            logger.error("Structures directory does not exist or is not a directory: {}", EnvStatics.STRUCTURE_DATA_PATH);
            return;
        }

        File[] files = structuresDir.listFiles();
        if (files == null) {
            logger.info("No structures found in: {}, skipping structure loading.", EnvStatics.STRUCTURE_DATA_PATH);
            return;
        }

        HashMap<String, List<Structure>> foundStructures = scanStructureFolderRecursive(structuresDir, "", 0);
        int totalStructuresFound = foundStructures.values().stream().mapToInt(List::size).sum();

        structures.clear();
        structures.putAll(foundStructures);

        logger.info("Finished scanning structures. Found {} structures.", totalStructuresFound);
    }

    private HashMap<String, List<Structure>> scanStructureFolderRecursive(File folder, String folderPath, int depth) {
        HashMap<String, List<Structure>> foundStructures = new HashMap<>();
        if (!folder.exists() || !folder.isDirectory()) return foundStructures;

        if (!folder.getName().matches(STRUCTURE_NAME_REGEX)) {
            logger.warn("Invalid structure folder name (allowed: {}). Skipping folder: {}", STRUCTURE_NAME_REGEX, folder.getAbsolutePath());
            return foundStructures;
        }

        File[] files = folder.listFiles();
        if (files == null) return foundStructures;

        for (File file : files) {
            if (file.isDirectory()) {

                if (depth > MAX_FOLDER_DEPTH) {
                    logger.warn("Maximum folder depth reached while scanning structures. Skipping folder: {}", file.getAbsolutePath());
                    continue;
                }

                String subFolderPath = folderPath.isEmpty() ? file.getName() : folderPath + "/" + file.getName();
                HashMap<String, List<Structure>> subFolderStructures = scanStructureFolderRecursive(file, subFolderPath, depth + 1);

                for (String path : subFolderStructures.keySet()) {
                    foundStructures.putIfAbsent(path, new ArrayList<>());
                    foundStructures.get(path).addAll(subFolderStructures.get(path));
                }

                continue;
            }

            Structure structure = readStructureFromFile(file, folderPath);
            if (structure == null) continue;

            String path = structure.path();
            foundStructures.putIfAbsent(path, new ArrayList<>());
            foundStructures.get(path).add(structure);
        }

        return foundStructures;
    }

    private @Nullable Structure readStructureFromFile(File file, String path) {
        if (!file.isFile() || !file.getName().endsWith(STRUCTURE_FILE_EXTENSION)) {
            logger.warn("Invalid structure file (must be a {} file). Skipping file: {}", STRUCTURE_FILE_EXTENSION, file.getAbsolutePath());
            return null;
        }

        String id = file.getName().substring(0, file.getName().length() - 4);
        if (!id.matches(STRUCTURE_NAME_REGEX)) {
            logger.warn("Invalid structure name (allowed: {}). Skipping file: {}", STRUCTURE_NAME_REGEX, file.getAbsolutePath());
            return null;
        }

        try {
            CompoundBinaryTag structure = BinaryTagIO.reader().read(Path.of(file.getAbsolutePath()), BinaryTagIO.Compression.GZIP);
            StructureNbt structureNbt = StructureNbt.fromNbt(structure);

            if (structureNbt == null) {
                logger.warn("Invalid structure NBT data. Skipping file: {}", file.getAbsolutePath());
                return null;
            }

            return new Structure(id, path, structureNbt);
        } catch (IOException exception) {
            logger.error("Failed to read structure {}", path, exception);
            return null;
        }
    }
}
