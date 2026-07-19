package kr.ac.tukorea.bandi.domain.audit.service;

import kr.ac.tukorea.bandi.domain.audit.mapper.AuditLogMapper;
import kr.ac.tukorea.bandi.domain.audit.model.AuditAction;
import kr.ac.tukorea.bandi.domain.audit.model.AuditLog;
import kr.ac.tukorea.bandi.domain.audit.model.AuditTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogMapper auditLogMapper;
    private final Clock clock;

    @Transactional
    public void record(Long actorMemberId, AuditAction action,
                       AuditTargetType targetType, Long targetId,
                       String summary) {
        auditLogMapper.insert(AuditLog.record(actorMemberId, action,
                targetType, targetId, summary, LocalDateTime.now(clock)));
    }
}
