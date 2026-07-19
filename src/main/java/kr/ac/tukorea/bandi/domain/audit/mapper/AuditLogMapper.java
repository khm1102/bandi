package kr.ac.tukorea.bandi.domain.audit.mapper;

import kr.ac.tukorea.bandi.domain.audit.model.AuditLog;
import kr.ac.tukorea.bandi.domain.audit.model.AuditTargetType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuditLogMapper {

    List<AuditLog> searchByTarget(
            @Param("targetType") AuditTargetType targetType,
            @Param("targetId") Long targetId);

    int insert(AuditLog auditLog);
}
