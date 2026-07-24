package kr.ac.tukorea.bandi.domain.file.mapper;

import kr.ac.tukorea.bandi.domain.file.model.ProfilePhotoRetirementManifest;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProfilePhotoRetirementMapper {

    int insert(ProfilePhotoRetirementManifest manifest);

    List<ProfilePhotoRetirementManifest> searchUncompleted();

    int updateDeleted(@Param("manifestId") Long manifestId,
                      @Param("completedDttm") LocalDateTime completedDttm);

    int updateFailed(@Param("manifestId") Long manifestId,
                     @Param("failureReason") String failureReason);
}
