package nl.devpieter.features.parkour.enums;

public enum ParkourCancelReason {
    None(0),
    Unknown(1),
    PortalUsed(2),
    PlayerQuit(3),
    PlayerRequest(4);

    private final int value;

    ParkourCancelReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ParkourCancelReason fromInt(int value) {
        for (ParkourCancelReason reason : values()) if (reason.getValue() == value) return reason;
        return Unknown;
    }
}
