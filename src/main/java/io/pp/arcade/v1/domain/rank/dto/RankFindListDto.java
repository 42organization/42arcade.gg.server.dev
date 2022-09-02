package io.pp.arcade.v1.domain.rank.dto;

import io.pp.arcade.v1.global.type.GameType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
public class RankFindListDto {
    private Pageable pageable;
    private GameType gameType;
    private Integer count;

    @Override
    public String toString() {
        return "RankFindListDto{" +
                "pageable=" + pageable +
                ", gameType=" + gameType +
                ", count=" + count +
                '}';
    }
}
