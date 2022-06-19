package io.pp.arcade.domain.rank;


import io.pp.arcade.global.type.RacketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankRepository extends JpaRepository<Rank,Integer> {
    List<Rank> findAllBySeasonId(Integer seasonId);
}
