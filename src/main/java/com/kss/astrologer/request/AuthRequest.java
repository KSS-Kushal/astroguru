package com.kss.astrologer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^(\\d{10}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
            message = "Must be a valid 10-digit mobile number or email address"
    )
    private String mobile;
}
