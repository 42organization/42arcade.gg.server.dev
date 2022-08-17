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

    public void gameGenerator() throws MessagingException {
        LocalDateTime now = LocalDateTime.now();
        now = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), 0);
        SlotDto slotDto = slotService.findByTime(now);

        if (slotDto != null) {
            addGameOrNotiCanceled(slotDto);
        }
    }

    public void gameGenerator(LocalDateTime time) throws MessagingException {
        SlotDto slotDto = slotService.findByTime(time);

        if (slotDto != null) {
            addGameOrNotiCanceled(slotDto);
        }
    }

    private void addGameOrNotiCanceled(SlotDto slotDto) throws MessagingException {
        if (slotDto.getHeadCount().equals(getMaxHeadCount(slotDto.getType()))) {
            addGame(slotDto);
        } else {
            notiCanceled(slotDto);
        }
    }

    private Integer getMaxHeadCount(GameType type) {
        Integer maxHeadCount = 2;
        if (GameType.DOUBLE.equals(type)) {
            maxHeadCount = 4;
        }
        return maxHeadCount;
    }

    private void notiCanceled(SlotDto slotDto) throws MessagingException {
        NotiCanceledTypeDto canceledDto = NotiCanceledTypeDto.builder().slotDto(slotDto).notiType(NotiType.CANCELEDBYTIME).build();
        notiGenerater.addCancelNotisBySlot(canceledDto);

        CurrentMatchRemoveDto removeDto = CurrentMatchRemoveDto.builder()
                .slotId(slotDto.getId())
                .build();
        currentMatchService.removeCurrentMatch(removeDto);
    }

    private void addGame(SlotDto slotDto) {
        GameAddDto gameAddDto = GameAddDto.builder()
                .slotDto(slotDto)
                .build();
        gameService.addGame(gameAddDto);
        GameDto game = gameService.findBySlot(slotDto.getId());

        CurrentMatchSaveGameDto matchSaveGameDto = CurrentMatchSaveGameDto.builder()
                .gameId(game.getId())
                .build();
        currentMatchService.saveGameInCurrentMatch(matchSaveGameDto);
    }

    public void gameLiveToWait() {
        LocalDateTime now = LocalDateTime.now();
        now = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), 0).minusMinutes(interval);
        SlotDto slot = slotService.findByTime(now);
        if (slot != null) {
            GameDto game = gameService.findBySlotIdNullable(slot.getId());
            if (game != null && game.getStatus().equals(StatusType.LIVE)) {
                gameService.modifyGameStatus(GameModifyStatusDto.builder().gameId(game.getId()).status(StatusType.WAIT).build());
            }
        }
    }

    private void saveCurrentMatch(UserDto user, GameDto game) {
        if (user != null) {
        }
    }

    private void removeCurrentMatch(UserDto user) {
        if (user != null) {
        }
    }

    @Override
    public Runnable runnable() {
        return () -> {
            try {
                gameGenerator();
                gameLiveToWait();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        };
    }
}
