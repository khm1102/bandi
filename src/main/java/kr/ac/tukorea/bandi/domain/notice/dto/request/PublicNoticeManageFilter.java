package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;

public record PublicNoticeManageFilter(PublicNoticeStatus status) {
}
