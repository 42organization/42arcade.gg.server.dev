package io.pp.arcade.v1.domain.game.Manager;

import io.pp.arcade.v1.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchDto;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchFindDto;
import io.pp.arcade.v1.domain.currentmatch.dto.CurrentMatchRemoveDto;
import io.pp.arcade.v1.domain.game.GameService;
import io.pp.arcade.v1.domain.game.dto.GameDto;
import io.pp.arcade.v1.domain.game.dto.GameModifyStatusDto;
import io.pp.arcade.v1.domain.game.dto.GameResultRequestDto;
import io.pp.arcade.v1.domain.pchange.PChangeService;
import io.pp.arcade.v1.domain.pchange.dto.PChangeAddDto;
import io.pp.arcade.v1.domain.rank.dto.RankUpdateDto;
import io.pp.arcade.v1.domain.rank.service.RankRedisService;
import io.pp.arcade.v1.domain.slot.dto.SlotDto;
import io.pp.arcade.v1.domain.slotteamuser.SlotTeamUserService;
import io.pp.arcade.v1.domain.slotteamuser.dto.SlotTeamUserDto;
import io.pp.arcade.v1.domain.team.TeamService;
import io.pp.arcade.v1.domain.team.dto.TeamDto;
import io.pp.arcade.v1.domain.team.dto.TeamModifyGameResultDto;
import io.pp.arcade.v1.domain.team.dto.TeamPosDto;
import io.pp.arcade.v1.domain.user.UserService;
import io.pp.arcade.v1.domain.user.dto.UserDto;
import io.pp.arcade.v1.domain.user.dto.UserModifyExpDto;
import io.pp.arcade.v1.domain.user.dto.UserModifyPppDto;
import io.pp.arcade.v1.global.exception.BusinessException;
import io.pp.arcade.v1.global.redis.Key;
import io.pp.arcade.v1.global.type.StatusType;
import io.pp.arcade.v1.global.util.EloRating;
import io.pp.arcade.v1.global.util.ExpLevelCalculator;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
public class GameManager {
    private final GameService gameService;
    private final SlotTeamUserService slotTeamUserService;
    private final TeamService teamService;
    private final UserService userService;
    private final PChangeService pChangeService;
    private final CurrentMatchService currentMatchService;
    private final RankRedisService rankRedisService;
    private final RedisTemplate<String, Integer> redisTemplate;


    public void checkIfUserHavePlayingGame(CurrentMatchDto currentMatch) {
        if (currentMatch == null) {
            throw new BusinessException("E0001");
        }
        GameDto game = currentMatch.getGame();
        if (game == null) {
            throw new BusinessException("E0001");
        }
    }

    public void endGameStatus(GameDto game) {
        GameModifyStatusDto modifyStatusDto = GameModifyStatusDto.builder()
                .gameId(game.getId())
                .status(StatusType.END).build();
        gameService.modifyGameStatus(modifyStatusDto);
    }

    public void modifyTeams(GameDto game, GameResultRequestDto requestDto, List<SlotTeamUserDto> slotTeamUsers) {
        Integer gamePpp = game.getSlot().getGamePpp();
        SlotDto slot = game.getSlot();
        Boolean isOneSide = Math.abs(requestDto.getMyTeamScore() - requestDto.getEnemyTeamScore()) == 2;

        for(SlotTeamUserDto slotTeamUser : slotTeamUsers) {
            TeamDto team;
            TeamModifyGameResultDto teamModifyGameResultDto;
            Integer enemyPpp;
            Integer pppChange;
            Integer score;
            Boolean isWin;
            TeamPosDto teamPosDto = teamService.findUsersByTeamPos(slotTeamUser.getSlot(), slotTeamUser.getUser());

            if (teamPosDto.getMyTeam().equals(slotTeamUser.getTeam())) {
                enemyPpp = (gamePpp * 2 - slotTeamUser.getTeam().getTeamPpp());
                team = slotTeamUser.getTeam();
                isWin = requestDto.getMyTeamScore() > requestDto.getEnemyTeamScore();
                score = requestDto.getMyTeamScore();
            } else {
                isWin = requestDto.getMyTeamScore() < requestDto.getEnemyTeamScore();
                enemyPpp = slotTeamUser.getTeam().getTeamPpp();
                team = slotTeamUser.getTeam();
                score = requestDto.getEnemyTeamScore();
            }
            pppChange = EloRating.pppChange(slotTeamUser.getUser().getPpp(), enemyPpp, isWin, isOneSide);
            teamModifyGameResultDto = TeamModifyGameResultDto.builder()
                    .teamId(team.getId())
                    .score(score)
                    .win(isWin)
                    .build();
            UserModifyPppDto modifyPppDto = UserModifyPppDto.builder()
                    .userId(slotTeamUser.getUser().getId())
                    .ppp(slotTeamUser.getUser().getPpp() + pppChange)
                    .build();
            PChangeAddDto pChangeAddDto = PChangeAddDto.builder()
                    .gameId(game.getId())
                    .userId(slotTeamUser.getUser().getId())
                    .pppChange(pppChange)
                    .pppResult(slotTeamUser.getUser().getPpp() + pppChange)
                    .build();
            RankUpdateDto rankUpdateDto =  RankUpdateDto.builder()
                    .gameType(slotTeamUser.getSlot().getType())
                    .Ppp(slotTeamUser.getUser().getPpp() + pppChange)
                    .intraId(slotTeamUser.getUser().getIntraId())
                    .isWin(isWin)
                    .build();
            teamService.modifyGameResultInTeam(teamModifyGameResultDto);
            rankRedisService.updateUserPpp(rankUpdateDto);
            userService.modifyUserPpp(modifyPppDto);
            pChangeService.addPChange(pChangeAddDto);
        }
    }

    public void removeCurrentMatch(GameDto game) {
        CurrentMatchFindDto findDto = CurrentMatchFindDto.builder()
                .game(game)
                .build();
        List<CurrentMatchDto> currentMatchDtos = currentMatchService.findCurrentMatchByGame(findDto);
        currentMatchDtos.forEach(currentMatchDto -> {
            CurrentMatchRemoveDto removeDto = CurrentMatchRemoveDto.builder()
                    .userId(currentMatchDto.getUser().getId()).build();
            currentMatchService.removeCurrentMatch(removeDto);
        });
    }

    public void modifyUserExp(UserDto user, GameDto game) {
        Integer gamePerDay = redisTemplate.opsForValue().get(Key.GAME_PER_DAY + user.getIntraId());
        gamePerDay = gamePerDay != null ? gamePerDay : 0;

        if (gamePerDay == 0) {
            redisTemplate.opsForValue().set(Key.GAME_PER_DAY + user.getIntraId(), 1, 3, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(Key.GAME_PER_DAY + user.getIntraId());
        }

        Integer expChange = ExpLevelCalculator.getExpPerGame() + ExpLevelCalculator.getExpBonus() * gamePerDay;

        pChangeService.addPChange(PChangeAddDto.builder().gameId(game.getId()).userId(user.getId()).expChange(expChange).expResult(user.getTotalExp() + expChange).build());
        userService.modifyUserExp(UserModifyExpDto.builder().userId(user.getId()).exp(expChange).build());
    }

    public CurrentMatchDto checkIfCurrentMatchExist(UserDto user) {
        CurrentMatchDto currentMatch = currentMatchService.findCurrentMatchByUser(user);
        // if the result already exists, throw 202 error
        if (currentMatch == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }
        return currentMatch;
    }
}