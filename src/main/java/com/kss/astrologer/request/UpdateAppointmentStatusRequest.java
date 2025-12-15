package com.kss.astrologer.request;

import com.kss.astrologer.types.BookingStatus;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {
    private Integer otp;
    private BookingStatus status;
}
