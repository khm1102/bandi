# bandi 데이터베이스 스키마

> 문서 상태: 1차 구현 기준
> 작성 기준일: 2026-07-22
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
| `member` | 학교 학번, 이름, 역할, 활동 상태, SSO 연결 상태 |
| `team` | 동아리 팀 마스터 |
| `cohort` | 기수 |
| `member_team_history` | 팀 변경 이력 |
| `member_role_history` | 역할 변경 이력 |
| `member_status_history` | 활동 상태 변경 이력 |
| `member_cohort_history` | 기수 변경 이력 |

현재 멤버는 하나의 팀만 참조한다. 학번은 재사용하지 않으며, 역할과 상태 변경은
이력 테이블에도 기록한다.

## 3. 일정과 콘텐츠

| 테이블 | 책임 |
| --- | --- |
| `calendar_event` | 전체·팀 일정 |
| `public_notice`, `public_notice_attachment` | 외부 공시와 첨부 |
| `internal_notice`, `internal_notice_attachment` | 내부 공지와 첨부 |
| `internal_notice_read` | 멤버별 공지 읽음 상태 |
| `resource`, `resource_file` | 자료와 리비전별 파일 연결 |
| `activity_record`, `activity_record_file` | 활동 기록과 증빙 파일 |
| `activity_record_revision`, `activity_review_history` | 활동 기록 수정·검토 이력 |

`public_notice.category_code`는 `GENERAL`, `RECRUITMENT`만 허용한다. 공지와 자료의
공개 범위는 전체 또는 팀 단위로 관리한다.

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
| `stored_file` | 파일명, MIME, 크기, SHA-256, 저장 키, 저장 상태 |
| `audit_log` | 주요 운영 변경의 처리자, 대상, 시각과 사유 |
| `SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES` | JDBC 세션 |

`stored_file.storage_key`는 애플리케이션이 생성한 상대 키만 허용한다. 파일 전송은
Spring Boot가 권한을 확인한 뒤 직접 스트리밍한다.

## 6. 마이그레이션 원칙

- 적용된 Flyway 파일은 수정하지 않는다.
- 스키마와 Java enum·`CHECK` 제약은 같은 변경 단위에서 동기화한다.
- 개발·테스트 스키마는 각각 `bandi`, `bandi_test`를 사용한다.
