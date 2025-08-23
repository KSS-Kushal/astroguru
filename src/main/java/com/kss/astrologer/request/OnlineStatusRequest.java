package com.kss.astrologer.request;

import com.kss.astrologer.types.OnlineType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStatusRequest {
    private OnlineType onlineType;
    private Boolean status;
}
