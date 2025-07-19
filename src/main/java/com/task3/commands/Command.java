package com.task3.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record Command(
        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Priority is required")
        CommandPriority priority,

        @NotBlank(message = "Author is required")
        @Size(max = 100, message = "Author name cannot exceed 100 characters")
        String author,

        @NotNull(message = "Time is required")
        Instant time
) {
}
