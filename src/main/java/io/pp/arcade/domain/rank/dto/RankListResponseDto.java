package io.pp.arcade.domain.rank.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class RankListResponseDto {
    private String myIntraId;
    private Integer myRank;
    private Integer currentPage;
    private Integer totalPage;
    private List<RankUserDto> rankList;
}
