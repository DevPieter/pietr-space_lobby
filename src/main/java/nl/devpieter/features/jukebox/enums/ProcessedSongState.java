package nl.devpieter.features.jukebox.enums;

public enum ProcessedSongState {
    Completed(0),
    Failed(1);

    private final int value;

    ProcessedSongState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
