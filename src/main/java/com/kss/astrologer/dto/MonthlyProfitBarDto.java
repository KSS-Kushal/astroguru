package com.kss.astrologer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyProfitBarDto {
    private String month; // Jan, Feb...
    private Double profit;
}
