package com.kss.astrologer.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBalanceRequest {
    private String mobile;
    private double amount;
}
