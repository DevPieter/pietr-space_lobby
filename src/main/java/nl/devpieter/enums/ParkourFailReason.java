package nl.devpieter.enums;

public enum ParkourFailReason {
    Unknown(0),
    Cancelled(1),
    PlayerFell(2),
    PlayerRequest(3);

    private final int value;

    ParkourFailReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ParkourFailReason fromInt(int value) {
        for (ParkourFailReason reason : values()) if (reason.getValue() == value) return reason;
        return Unknown;
    }
}
