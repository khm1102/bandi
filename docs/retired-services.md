# 폐기 서비스 이력

> 확정일: 2026-07-21
> 적용 PR: 서비스 폐기 PR, 로컬 디스크 파일 저장 전환 PR

다음 기능은 1차 제품 범위에서 제거한다. 이 문서는 과거 기획을 구현 근거로
사용하지 않도록 남기는 폐기 이력이며, 대체 기능을 정의하지 않는다.

| 폐기 기능 | 제거 범위 |
| --- | --- |
| 회비 | 화면, API, fee_item·fee_charge와 이력 |
| 행사·출석 | 화면, API, club_event·출석과 이력 |
| 공연 제작 | 프로젝트, 팀별 제작 진행, 체크리스트, 공연 콘텐츠 |
| 공연 운영·공개 홍보 | 공연 운영 설정, 외부 공연 페이지, 캐스팅·미디어·관람 안내·공연 공시 연결 |
| 관람 신청·입장 | 좌석, 신청, QR, 점유, 입장과 모든 이력 |
| 정책·공개 프로필 | 공연·관람 신청 전용 정책 문서와 공개 프로필 동의 |
| 프로젝트 기반 소품 사용 | asset_usage와 사용 예약·반납 흐름. 소품 품목·개별 장비·상태 이력은 유지 |

## URL과 API

내부 폐기 URL은 인증된 사용자가 접근하면 404가 된다.

- /dues, /attendance, /production, /checklist
- /performance-management, /performance-content-management
- /reservations, /showops

공개 공연·관람 URL과 공개 API는 로그인 화면으로 보내지 않고 404가 된다.

- /performances/**, /reserve/**
- /api/public-performances/**, /api/public-policies/**,
  /api/public-reservations/**

## 데이터

서비스 폐기 migration은 하위 관계와 이력부터 삭제하고 stored_file은 다음
파일 저장 전환 migration까지 유지한다. 이어지는 파일 저장 전환에서 기존
MinIO 객체와 파일이 연결된 공시·내부 공지·자료·활동 기록을 파기하며,
asset_item.photo_file_id만 NULL로 갱신해 소품 품목은 유지한다.

## 정본 문서 사용 규칙

- feature-spec.md와 database-schema.md의 폐기 기능 서술은 역사적 설계
  기록이다. 구현 근거로 사용하지 않는다.
- performance-operations-plan.md는 전체가 폐기 이력으로 전환됐다.
- 새 기능은 남은 범위와 실제 Flyway schema를 기준으로 별도 승인한다.
