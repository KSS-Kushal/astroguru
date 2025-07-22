package com.kss.astrologer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponseDto {
    private Long totalUsers;
    private Long totalAstrologers;
    private Double totalUserWalletBalance;
    private Double totalAstrologerWalletBalance;
}
