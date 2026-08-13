package nl.devpieter.features.maze;

public class MazeManager {

    private static MazeManager INSTANCE;

    private MazeManager() {
    }

    public static MazeManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MazeManager();
        return INSTANCE;
    }
}
