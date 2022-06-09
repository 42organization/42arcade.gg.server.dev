package io.pp.arcade.domain.slot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SlotAddRequestDto {
    private LocalDateTime time;
}
