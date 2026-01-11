package nl.devpieter.features.jukebox.enums;

public enum MusicPackJobState {
    Pending(0),
    Queued(1),
    Started(2),
    Completed(3),
    Failed(4);

    private final int value;

    MusicPackJobState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MusicPackJobState fromInt(int value) {
        for (MusicPackJobState state : MusicPackJobState.values()) {
            if (state.getValue() != value) continue;
            return state;
        }

        throw new IllegalArgumentException("Invalid MusicPackJobState value: " + value);
    }
}
