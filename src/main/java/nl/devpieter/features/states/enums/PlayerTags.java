package nl.devpieter.features.states.enums;

public enum PlayerTags {

    SITTING("sitting"),
    IN_PARKOUR("in_parkour"),
    IN_PARKOUR_REPLAY("in_parkour_replay");

    private final String tag;

    PlayerTags(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }
}
