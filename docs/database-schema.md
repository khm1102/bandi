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
| `member` | 학교 학번, 이름, SSO 동기화 휴대폰 번호, 역할, 활동 상태, SSO 연결 상태, 내부 프로필 사진 FK |
| `team` | 동아리 팀 마스터 |
| `cohort` | 기수 |
| `member_team_history` | 팀 변경 이력 |
| `member_role_history` | 역할 변경 이력 |
| `member_status_history` | 활동 상태 변경 이력 |
| `member_cohort_history` | 기수 변경 이력 |
| `club_officer` | 권한과 독립된 현재 동아리 직책 담당자 |

현재 멤버는 하나의 팀만 참조한다. 학번은 재사용하지 않으며, 역할과 상태 변경은
이력 테이블에도 기록한다. `cohort`는 `name`과 `is_active`만 가지는 문자열 기준정보로,
기존 기수와 이력을 보존하기 위해 삭제·비활성화하지 않고 신규 추가만 허용한다.
`member.phone_number`는 학교 SSO에서 받은 숫자만 저장하며, SSO 응답에 유효한 번호가 없으면
기존 값을 지우지 않는다. 이 값은 본인 프로필, 전체 멤버 관리 `ADMIN`, 현재 팀 멤버 관리
`LEADER`에게만 반환하며, 공지·자료·공유 응답에는 포함하지 않는다.

`club_officer.position_code`는 현재 `PRESIDENT`만 허용하며 직책별 한 명만 존재한다.
활동 내역서 생성은 이 테이블이 참조하는 활성 멤버 이름을 매 요청마다 조회한다.
HWPX 문서를 임시 저장할 때 현재 회장 이름으로 결과 파일을 생성한다.

## 3. 일정과 콘텐츠

| 테이블 | 책임 |
| --- | --- |
| `calendar_event` | 전체·팀 일정 |
| `internal_notice`, `internal_notice_attachment` | 내부 공지와 첨부, 선택적 제목 공개 공유 토큰 |
| `internal_notice_read` | 멤버별 공지 읽음 상태 |
| `resource`, `resource_file` | 공용 자료 Markdown 원문과 현재 첨부 파일 연결, 선택적 제목 공개 공유 토큰 |
| `resource_link_preview` | 자료 본문의 HTTPS URL Open Graph 스냅샷 |
| `resource_link_preview_retirement_manifest` | 더 이상 쓰이지 않는 링크 카드 대표 이미지 파기 재시도 |
| `activity_record`, `activity_record_file` | 활동 기록과 증빙 파일 |
| `activity_record_revision`, `activity_review_history` | 활동 기록 수정·검토 이력 |
| `activity_report_document` | HWPX 활동 내역서의 대표자·장소와 활동 기록 연결 |
| `activity_report_participant` | HWPX에 반영할 참여자 1~14명의 입력 스냅샷 |

내부 공지만 전체 또는 팀 단위의 공개 범위를 관리한다. 자료실은 활성 멤버 전체가 열람하는
공용 게시판이며 `resource.created_by_member_id`의 작성자와 `ADMIN`만 수정·소프트 삭제할 수 있다.

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

`internal_notice.share_token`과 `resource.share_token`은 각각 nullable unique `VARCHAR(43)`다.
256비트 난수를 Base64URL로 인코딩한 값만 저장하며, 일반 상세·목록 API에는 반환하지 않는다.
공지 토큰은 현재 `PUBLISHED` 상태·게시 기간 안에서만, 자료 토큰은 소프트 삭제 전까지만 유효하다.
공유 중단은 토큰을 `NULL`로 갱신해 기존 주소를 즉시 404로 만들며, 재발급은 새 토큰으로 이전
주소를 무효화한다.

`resource.body_markdown`은 Markdown 원문만 저장하며 렌더된 HTML은 저장하지 않는다.
`resource_file`은 현재 첨부 관계와 표시 순서만 관리한다. 단독 줄의 HTTPS URL은
`resource_link_preview`에 정규화 URL, 제목·설명 스냅샷과 선택 대표 이미지 파일을 저장한다.
대표 이미지는 private 로컬 저장소에 두며, 더 이상 참조되지 않으면
`resource_link_preview_retirement_manifest`를 통해 파일 객체와 `stored_file` 메타데이터를
재시도 가능하게 파기한다. 재시도는 `RESOURCE_LINK_PREVIEW_RETIREMENT_MODE=APPLY` 기동에서만
수행한다.

`calendar_event`의 유효 기간은 `end_dttm > start_dttm`이며, 기간 조회는
`start_dttm < rangeEnd AND end_dttm > rangeStart`의 반개구간으로 겹침을 판정한다.
종일 일정은 시작일 00:00을 포함하고 사용자가 선택한 마지막 날 다음 날 00:00을
제외하는 배타적 종료값을 저장한다. 설명과 장소는 선택값이며 제목은 최대 150자다.
`color_code`는 `NAVY`, `MINT`, `BLUE`, `PLUM`, `AMBER`, `ROSE` 중 하나를 저장하며,
기존·미지정 일정의 기본값은 `NAVY`다. 색상은 일정의 보조 구분 정보이므로 팀 범위와
제목을 함께 표시한다.

HWPX 활동 내역서는 `activity_record`를 검수 상태의 정본으로 사용한다. 간단 기록은
사진 없이 제출할 수 있지만 HWPX 기록은 `EVIDENCE`와 `DOCUMENT` 현재 파일이 모두 있어야
제출할 수 있다. 검수 상태는 `DRAFT`, `SUBMITTED`, `TEAM_APPROVED`, `APPROVED`,
`REVISION_REQUESTED`, `ARCHIVED`이며, `activity_review_history`가 팀장 1차 승인·관리자
최종 승인·긴급 승인 사유를 기록한다. 사용자가 입력한
활동 기록 제목은 `activity_record.title`에 저장하며, 월별 고정 HWPX 양식 제목과 분리한다. 사진은
`activity_record_file.file_role_code = 'EVIDENCE'`, 생성 문서는 `DOCUMENT`로 연결하며
두 바이너리 모두 `stored_file` 메타데이터와 로컬 private 저장소에 보관한다. 대표자·장소와
참여자 입력은 문서 재생성을 위해 별도 테이블에 저장한다. `임시 저장`은 `DRAFT`,
`검수 요청`은 `SUBMITTED` 전이를 사용한다. 팀장은 소속 팀 기록만 1차 승인하고, 관리자는
전체 팀 기록을 최종 승인하거나 긴급 승인할 수 있다. 팀장은 본인 기록을 1차 검수할 수 없고,
관리자는 본인이 작성한 기록도 최종 검수·보관할 수 있다.

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
| `resource_link_preview_retirement_manifest` | 자료 링크 카드 대표 이미지의 물리 파일·메타데이터 파기 재시도 |
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
