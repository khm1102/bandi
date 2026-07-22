package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

public record InternalNoticeManageFilter(
        InternalNoticeStatus status,
        InternalNoticeTargetScope targetScope
) {
}
