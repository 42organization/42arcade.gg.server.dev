package io.pp.arcade.domain.team.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TeamPosDto {
    TeamDto myTeam;
    TeamDto enemyTeam;
}
