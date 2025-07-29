package com.kss.astrologer.services.sms;

public interface SmsService {
    public String sendSms(String phoneNumber, String otp);
}
