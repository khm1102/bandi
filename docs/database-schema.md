# bandi 데이터베이스 스키마

> 문서 상태: 1차 구현 기준
> 작성 기준일: 2026-07-23
> DBMS: MySQL 8

실제 스키마 변경은 `src/main/resources/db/migration`의 Flyway 파일만으로 수행한다.
이 문서는 현재 유지 기능의 데이터 책임과 제약을 설명한다.

## 1. 공통 규칙

- 테이블과 컬럼은 소문자 snake_case를 사용하고 PK는 `{table}_id`로 둔다.
- 상태·분류 코드는 MySQL `ENUM`이 아니라 `VARCHAR`와 Java enum, `CHECK` 제약을
  함께 사용한다.
- 업무 테이블은 `created_dttm`, `updated_dttm`을 기록한다.
- 소프트 삭제 테이블 조회에는 `deleted_dttm IS NULL` 조건을 사용한다.
- 파일 바이너리는 DB에 저장하지 않는다. `stored_file`에는 메타데이터와 저장 키만
  보관한다.

## 2. 멤버와 조직

| 테이블 | 책임 |
| --- | --- |
| `member` | 학교 학번, 이름, 역할, 활동 상태, SSO 연결 상태, 내부 프로필 사진 FK |
| `team` | 동아리 팀 마스터 |
| `cohort` | 기수 |
| `member_team_history` | 팀 변경 이력 |
| `member_role_history` | 역할 변경 이력 |
| `member_status_history` | 활동 상태 변경 이력 |
| `member_cohort_history` | 기수 변경 이력 |
| `club_officer` | 권한과 독립된 현재 동아리 직책 담당자 |

현재 멤버는 하나의 팀만 참조한다. 학번은 재사용하지 않으며, 역할과 상태 변경은
이력 테이블에도 기록한다.

`club_officer.position_code`는 현재 `PRESIDENT`만 허용하며 직책별 한 명만 존재한다.
활동 내역서 생성은 이 테이블이 참조하는 활성 멤버 이름을 매 요청마다 조회한다.
HWPX 문서를 임시 저장할 때 현재 회장 이름으로 결과 파일을 생성한다.

## 3. 일정과 콘텐츠

| 테이블 | 책임 |
| --- | --- |
| `calendar_event` | 전체·팀 일정 |
| `internal_notice`, `internal_notice_attachment` | 내부 공지와 첨부 |
| `internal_notice_read` | 멤버별 공지 읽음 상태 |
| `resource`, `resource_file` | 자료와 리비전별 파일 연결 |
| `activity_record`, `activity_record_file` | 활동 기록과 증빙 파일 |
| `activity_record_revision`, `activity_review_history` | 활동 기록 수정·검토 이력 |
| `activity_report_document` | HWPX 활동 내역서의 대표자·장소와 활동 기록 연결 |
| `activity_report_participant` | HWPX에 반영할 참여자 1~14명의 입력 스냅샷 |

내부 공지와 자료의 공개 범위는 전체 또는 팀 단위로 관리한다.

`internal_notice.body`는 Markdown 원문이다. 렌더된 HTML·조회수는 저장하지 않으며,
공지 상세를 열 때 `internal_notice_read`를 upsert해 멤버별 읽음 상태만 보관한다.
`internal_notice_attachment`는 현재 연결만 보관한다. 수정에서 분리된 private 파일은
파일 feature의 미연결 파일 정리 대상으로 남긴다.

`internal_notice.status_code`는 `DRAFT`, `SCHEDULED`, `PUBLISHED`, `CLOSED`,
`ARCHIVED`를 사용한다. 보관은 `CLOSED`에서만, 초안 복귀는 `SCHEDULED`·`ARCHIVED`에서만
허용한다. 초안 복귀는 게시 시각·게시자와 기존 `internal_notice_read`를 초기화한다.
`DRAFT` 삭제는 `deleted_dttm`을 기록하는 소프트 삭제이며 첨부 연결과 파일 메타데이터는
미연결 파일 정리 정책이 마련될 때까지 유지한다. 모든 공지 조회는 `deleted_dttm IS NULL`을
강제한다.

`calendar_event`의 유효 기간은 `end_dttm > start_dttm`이며, 기간 조회는
`start_dttm < rangeEnd AND end_dttm > rangeStart`의 반개구간으로 겹침을 판정한다.
종일 일정은 시작일 00:00을 포함하고 사용자가 선택한 마지막 날 다음 날 00:00을
제외하는 배타적 종료값을 저장한다. 설명과 장소는 선택값이며 제목은 최대 150자다.
`color_code`는 `NAVY`, `MINT`, `BLUE`, `PLUM`, `AMBER`, `ROSE` 중 하나를 저장하며,
기존·미지정 일정의 기본값은 `NAVY`다. 색상은 일정의 보조 구분 정보이므로 팀 범위와
제목을 함께 표시한다.

HWPX 활동 내역서는 `activity_record`를 검수 상태의 정본으로 사용한다. 사진은
`activity_record_file.file_role_code = 'EVIDENCE'`, 생성 문서는 `DOCUMENT`로 연결하며
두 바이너리 모두 `stored_file` 메타데이터와 로컬 private 저장소에 보관한다. 대표자·장소와
참여자 입력은 문서 재생성을 위해 별도 테이블에 저장한다. `임시 저장`은 `DRAFT`,
`검수 요청`은 기존 활동 기록의 `SUBMITTED` 전이를 사용하고 운영진 검토·수정 요청·승인은
기존 활동 기록 검수 흐름을 그대로 따른다.

## 4. 소품·장비

| 테이블 | 책임 |
| --- | --- |
| `asset_item` | 수량형 품목과 기본 정보 |
| `asset_unit` | 개별 관리 장비 |
| `asset_history` | 상태·위치 변경 이력 |

사진 연결은 `asset_item.photo_file_id`로 선택적으로 보관한다.

## 5. 파일과 감사

| 테이블 | 책임 |
| --- | --- |
| `stored_file` | 파일명, MIME, 크기, SHA-256, 저장 키, 저장 상태, 파일 목적 |
| `member_profile_photo_retirement_manifest` | 교체·삭제된 내부 프로필 사진의 물리 파일·메타데이터 파기 재시도 |
| `audit_log` | 주요 운영 변경의 처리자, 대상, 시각과 사유 |
| `SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES` | JDBC 세션 |

`stored_file.storage_key`는 애플리케이션이 생성한 상대 키만 허용한다. 파일 전송은
Spring Boot가 권한을 확인한 뒤 직접 스트리밍한다.

`member.profile_photo_file_id`는 `stored_file.file_purpose_code = PROFILE_IMAGE`인
private 파일만 참조한다. 사진을 교체하거나 삭제할 때 먼저 FK를 해제하고 retirement
manifest를 남긴다. 로컬 객체 삭제와 `stored_file` 하드 삭제가 모두 끝난 경우에만
`DELETED`로 완료하며, 실패는 `FAILED`로 남아
`PROFILE_PHOTO_RETIREMENT_MODE=APPLY` 기동에서 재시도한다.

## 6. 마이그레이션 원칙

- 적용된 Flyway 파일은 수정하지 않는다.
- 스키마와 Java enum·`CHECK` 제약은 같은 변경 단위에서 동기화한다.
- 개발·테스트 스키마는 각각 `bandi`, `bandi_test`를 사용한다.
