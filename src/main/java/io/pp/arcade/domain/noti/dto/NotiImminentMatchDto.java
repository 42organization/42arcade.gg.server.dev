package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Getter
public class NotiImminentMatchDto {
    private Integer id;
    private String type;
    private LocalDateTime time;
    private Boolean isChecked;
    private List<String> myTeam;
    private List<String> enemyTeam;
    private LocalDateTime createdAt;

    @Builder
    public NotiImminentMatchDto(Integer id, NotiType type, LocalDateTime time, Boolean isChecked, List<String> myTeam, List<String> enemyTeam, LocalDateTime createdAt) {
        this.id = id;
        this.type = type.getCode().toLowerCase(Locale.ROOT);
        this.time = time;
        this.isChecked = isChecked;
        this.myTeam = myTeam;
        this.enemyTeam = enemyTeam;
        this.createdAt = createdAt;
    }
}
