package com.team404.synco.virtualmeeting.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveKitTokenResponseDto {
    private String token;
}
