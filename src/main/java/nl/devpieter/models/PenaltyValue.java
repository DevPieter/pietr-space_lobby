package nl.devpieter.models;

import java.util.HashMap;

public class PenaltyValue {

    public enum Type {
        ADDITIVE_NUMBER,
        ADDITIVE_PERCENTAGE,
        SUBTRACTIVE_NUMBER,
        SUBTRACTIVE_PERCENTAGE,
        MULTIPLICATIVE_NUMBER,
        MULTIPLICATIVE_PERCENTAGE
    }

    record Penalty(Type type, double value) {
    }

    private final HashMap<String, Penalty> penalties = new HashMap<>();

    private final boolean clamp;
    private final double minValue;
    private final double maxValue;

    private final double baseValue;
    private double value;
    private boolean isDirty = true;

    public PenaltyValue(double baseValue) {
        this(baseValue, false, 0, 0);
    }

    public PenaltyValue(double baseValue, double minValue, double maxValue) {
        this(baseValue, true, minValue, maxValue);
    }

    public PenaltyValue(double baseValue, boolean clamp, double minValue, double maxValue) {
        this.baseValue = baseValue;
        this.clamp = clamp;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public void addPenalty(String key, Type type, double value) {
        penalties.put(key, new Penalty(type, value));
        isDirty = true;
    }

    public void removePenalty(String key) {
        penalties.remove(key);
        isDirty = true;
    }

    public double getValue() {
        if (isDirty) {
            value = calculateRawValue();
            isDirty = false;
        }

        if (clamp) return Math.clamp(value, minValue, maxValue);
        return value;
    }

    public double calculateRawValue() {
        double value = baseValue;

        for (Penalty penalty : penalties.values()) {
            switch (penalty.type) {
                case ADDITIVE_NUMBER -> value += penalty.value;
                case ADDITIVE_PERCENTAGE -> value += baseValue * (penalty.value / 100);
                case SUBTRACTIVE_NUMBER -> value -= penalty.value;
                case SUBTRACTIVE_PERCENTAGE -> value -= baseValue * (penalty.value / 100);
                case MULTIPLICATIVE_NUMBER -> value *= penalty.value;
                case MULTIPLICATIVE_PERCENTAGE -> value *= (1 + penalty.value / 100);
            }
        }

        return value;
    }
}