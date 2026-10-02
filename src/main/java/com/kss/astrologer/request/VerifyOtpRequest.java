package com.kss.astrologer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^(\\d{10}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
            message = "Must be a valid 10-digit mobile number or email address"
    )
    private String mobile;

    @NotBlank(message = "OTP is required")
    @Size(min = 4, message = "OTP must be 4 digits")
    private String otp;
}
