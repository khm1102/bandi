package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.Team;

import java.util.List;
import java.util.Optional;

public interface TeamMapper {

    Optional<Team> lookupById(Long teamId);

    List<Team> searchAll();

    boolean existsByName(String name);

    int insert(Team team);

    int updateActive(Long teamId, boolean active);
}
