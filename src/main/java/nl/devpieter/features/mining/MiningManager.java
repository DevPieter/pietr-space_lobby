package nl.devpieter.features.mining;

public class MiningManager {

    private static MiningManager INSTANCE;

    private MiningManager() {
    }

    public static MiningManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MiningManager();
        return INSTANCE;
    }
}
