package io.pp.arcade.global.scheduler;

import io.pp.arcade.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchRemoveDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchSaveGameDto;
import io.pp.arcade.domain.game.GameService;
import io.pp.arcade.domain.game.dto.GameAddDto;
import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.game.dto.GameModifyStatusDto;
import io.pp.arcade.domain.noti.dto.NotiCanceledTypeDto;
import io.pp.arcade.domain.slot.SlotService;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.NotiType;
import io.pp.arcade.global.type.StatusType;
import io.pp.arcade.global.util.NotiGenerater;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.time.LocalDateTime;

//private String cron = "0 */" + intervalTime + " " + startTime + "-" + endTime + " * * *";
//private String cron = "0 */10 * * * *";
@Component
public class GameGenerator extends AbstractScheduler {
    private final GameService gameService;
    private final SlotService slotService;
    private final CurrentMatchService currentMatchService;
    private final NotiGenerater notiGenerater;

    public GameGenerator(GameService gameService, SlotService slotService, CurrentMatchService currentMatchService, NotiGenerater notiGenerater) {
        this.gameService = gameService;
        this.slotService = slotService;
        this.currentMatchService = currentMatchService;
        this.notiGenerater = notiGenerater;
        this.setCron("0 */10 * * * *");
        this.setInterval(10);
    }

    public void addGame() throws MessagingException {
        Integer maxHeadCount = 2;
        LocalDateTime now = LocalDateTime.now();
        now = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), 0);
        SlotDto slotDto = slotService.findByTime(now);

        if (slotDto != null && GameType.DOUBLE.equals(slotDto.getType())) {
            maxHeadCount = 4;
        }

        if (slotDto != null) {
            TeamDto team1 = slotDto.getTeam1();
            TeamDto team2 = slotDto.getTeam2();
            if (slotDto.getHeadCount().equals(maxHeadCount)) {

                GameAddDto gameAddDto = GameAddDto.builder()
                        .slotDto(slotDto)
                        .build();
                gameService.addGame(gameAddDto);
                GameDto game = gameService.findBySlot(slotDto.getId());

                saveCurrentMatch(team1.getUser1(), game);
                saveCurrentMatch(team1.getUser2(), game);
                saveCurrentMatch(team2.getUser1(), game);
                saveCurrentMatch(team2.getUser2(), game);
            } else {

                NotiCanceledTypeDto canceledDto = NotiCanceledTypeDto.builder().slotDto(slotDto).notiType(NotiType.CANCELEDBYTIME).build();
                notiGenerater.addCancelNotisBySlot(canceledDto);

                removeCurrentMatch(team1.getUser1());
                removeCurrentMatch(team1.getUser2());
                removeCurrentMatch(team2.getUser1());
                removeCurrentMatch(team2.getUser2());
            }
        }
    }

    public void gameLiveToWait() {
        LocalDateTime now = LocalDateTime.now();
        now = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), 0).minusMinutes(interval);
        SlotDto slot = slotService.findByTime(now);
        if (slot != null) {
            GameDto game = gameService.findBySlotNullable(slot.getId());
            if (game != null && game.getStatus().equals(StatusType.LIVE)) {
                gameService.modifyGameStatus(GameModifyStatusDto.builder().gameId(game.getId()).status(StatusType.WAIT).build());
            }
        }
    }

    private void saveCurrentMatch(UserDto user, GameDto game) {
        if (user != null) {
            CurrentMatchSaveGameDto matchSaveGameDto = CurrentMatchSaveGameDto.builder()
                    .gameId(game.getId())
                    .userId(user.getId())
                    .build();
            currentMatchService.saveGameInCurrentMatch(matchSaveGameDto);
        }
    }

    private void removeCurrentMatch(UserDto user) {
        if (user != null) {
            CurrentMatchRemoveDto removeDto = CurrentMatchRemoveDto.builder()
                    .userId(user.getId())
                    .build();
            currentMatchService.removeCurrentMatch(removeDto);
        }
    }

    @Override
    public Runnable runnable() {
        return () -> {
            try {
                addGame();
                gameLiveToWait();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        };
    }
}
