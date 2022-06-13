package io.pp.arcade.domain.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameUserInfoResponseDto {
    List<GameUserInfoDto> myTeam;
    List<GameUserInfoDto> enemyTeam;
}