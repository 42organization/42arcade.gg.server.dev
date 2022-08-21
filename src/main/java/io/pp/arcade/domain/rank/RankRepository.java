package io.pp.arcade.domain.rank;


import io.pp.arcade.global.type.RacketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank,Integer> {
    List<Rank> findAllBySeasonId(Integer seasonId);
    Optional<Rank> findBySeasonIdAndUserId(Integer seasonId, Integer userId);
    Page<Rank> findAllByOrderByIdDesc(Pageable pageable);
}
