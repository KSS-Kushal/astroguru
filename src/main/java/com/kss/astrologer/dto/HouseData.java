package com.kss.astrologer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseData {
    public Integer sign_no;
    public List<Planet> planet;
}
