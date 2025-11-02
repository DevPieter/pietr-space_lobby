package nl.devpieter.features.economy.skills.models;

public record ExperienceCalculationResult(
        double experience,
        int level,
        int prestige,
        double progressPercentage
) {
}
