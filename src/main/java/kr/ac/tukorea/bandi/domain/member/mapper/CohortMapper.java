package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface CohortMapper {

    Optional<Cohort> lookupById(Long cohortId);

    List<Cohort> searchAll();

    boolean existsByAdmissionYearAndTermCode(@Param("admissionYear") short admissionYear,
                                             @Param("termCode") String termCode);

    boolean existsByName(String name);

    int insert(Cohort cohort);
}
