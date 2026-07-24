package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.Cohort;

import java.util.List;
import java.util.Optional;

public interface CohortMapper {

    Optional<Cohort> lookupById(Long cohortId);

    List<Cohort> searchAll();

    boolean existsByName(String name);

    int insert(Cohort cohort);
}
