package com.reliaquest.api.controller.api;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateEmployeeRequest {

    @NotBlank
    String name;

    @Positive @NotNull Integer salary;

    @Min(16)
    @Max(75)
    @NotNull Integer age;

    @NotBlank
    String title;
}
