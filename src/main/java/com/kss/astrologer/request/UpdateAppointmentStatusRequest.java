package com.kss.astrologer.request;

import com.kss.astrologer.types.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentStatusRequest {
    private BookingStatus status;
    private Integer otp;
}
