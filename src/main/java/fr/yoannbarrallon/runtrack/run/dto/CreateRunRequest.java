package fr.yoannbarrallon.runtrack.run.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record CreateRunRequest(
        @NotBlank String title,
        @NotNull Instant startTime,
        @NotNull @Positive Integer durationSeconds,
        @NotNull @Positive Integer distanceMeters,
        @NotNull @PositiveOrZero Integer elevationGainMeters
) {
}
