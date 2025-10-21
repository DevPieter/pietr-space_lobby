package nl.devpieter.models;

public record YawPitch(float yaw, float pitch) {

    public static YawPitch ZERO = new YawPitch(0f, 0f);
}
