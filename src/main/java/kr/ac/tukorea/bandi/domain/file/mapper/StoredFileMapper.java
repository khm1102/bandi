package kr.ac.tukorea.bandi.domain.file.mapper;

import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface StoredFileMapper {

    Optional<StoredFile> lookupById(Long storedFileId);

    Optional<StoredFile> lookupByIdForUpdate(Long storedFileId);

    int insert(StoredFile storedFile);

    int updateReady(@Param("storedFileId") Long storedFileId,
                    @Param("objectEtag") String objectEtag);

    int updateFailed(Long storedFileId);
}
