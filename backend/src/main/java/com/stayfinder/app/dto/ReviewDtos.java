package com.stayfinder.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ReviewDtos {
    private ReviewDtos() {}

    public record ReviewRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 1200) String comment
    ) {}
}
