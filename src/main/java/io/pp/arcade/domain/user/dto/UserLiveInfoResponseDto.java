package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserLiveInfoResponseDto { 
    Integer notiCount;
    String event;
}
