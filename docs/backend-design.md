# bandi 백엔드 설계

> 문서 상태: 현행 구현 기준
> 기준 문서: `feature-spec.md`, `database-schema.md`, `coding-convention.md`
> 작성 기준일: 2026-07-23

## 1. 목적

bandi는 학교 SSO로 확인된 동아리 멤버가 일정, 공지·자료, 활동 기록,
소품·장비와 멤버 권한을 운영하는 SSR 웹 서비스다.

학교 비밀번호와 학교 세션은 인증 요청 중에만 사용하며, DB·세션·로그에 저장하지
않는다. 파일 바이너리는 `FILE_STORAGE_ROOT` 아래 로컬 영구 볼륨에 저장하고,
DB에는 메타데이터만 보관한다.

## 2. 구조

```text
Controller → Service → Mapper → Model
                     ↓
              다른 feature의 Service
```

- Controller는 HTTP 요청 검증, 로그인 멤버 식별, SSR 뷰 또는 API 응답을 담당한다.
- Service는 트랜잭션, 권한 범위와 유스케이스 흐름을 담당한다.
- Mapper는 자기 feature의 영속성만 담당한다.
- Model은 상태와 값 검증을 담당한다.
- 인증의 URL 경계는 Spring Security가, 팀·대상·상태 기반 권한은 Service가 최종
  검증한다.

## 3. 현행 feature

| feature | 책임 |
| --- | --- |
| `member` | 멤버 사전 등록, 학교 SSO 연결·휴대폰 동기화, 내 프로필, 팀·기수·역할·상태와 이력 |
| `calendar` | 전체·팀 일정 관리 |
| `notice` | 내부 공지, 읽음 상태 및 첨부 연결 |
| `resource` | 공용 Markdown 자료, 현재 첨부와 서버 수집 링크 카드 |
| `activity` | 팀 활동 기록·검토 이력과 저장·검수형 HWPX 활동 내역서 생성 |
| `asset` | 소품·장비 품목, 개별 장비, 상태 이력과 사진 연결 |
| `file` | 파일 메타데이터와 로컬 파일 저장·전송, 프로필 사진 파기 재시도 |
| `dashboard` | 각 feature의 읽기 전용 요약 |
| `audit` | 주요 운영 변경 감사 기록 |

## 4. HTTP 경계

- 페이지 조회는 JSP SSR Controller를 사용한다.
- 부분 갱신과 상태 변경은 `/api/**`에서 세션 인증과 CSRF 보호를 적용한다.
- 공지·공지 관리·자료실·멤버·팀 멤버 목록은 공통 `PageResponse<T>`로 응답한다.
  API의 `page`는 0부터 시작하고 `pageSize`는 최대 100이며, 제품 화면은 20건으로 고정한다.
  목록과 `COUNT(*)` 쿼리는 검색·권한·소프트 삭제 조건을 같은 SQL 조각으로 공유한다.
- Swagger 계약은 `global.swagger` 인터페이스에만 둔다.
- 파일은 Spring Boot가 권한을 확인한 뒤 직접 스트리밍한다.
- `/profile`과 `/api/members/me/**`는 로그인 멤버가 사용한다. `/team-members`와
  `/api/members/team-members`는 `LEADER`만 URL 단계에서 허용하고, 현재 소속 팀 범위는
  `MemberProfileService`가 다시 검사한다. `ADMIN`의 전체 멤버·팀·기수 관리는 `/members`에
  집중한다.
- `/notices`는 인증 멤버의 내부 공지 목록·상세 화면이며, `/notices/write`와
  `/notices/{id}/edit`, `/notices/manage`, `/notices/manage/{id}`는 `LEADER`·`ADMIN`만
  URL 단계에서 허용한다. 일반 상세의 `canManage`도 실제 공지 대상과 로그인 멤버의 팀
  범위를 Service에서 계산하며, 버튼 노출과 관계없이 모든 관리 API가 같은 범위를 다시 검사한다.
- 공지 상태 API는 게시·종료·보관 외에 `POST /api/internal-notice-management/{id}/draft`와
  `DELETE /api/internal-notice-management/{id}`를 제공한다. 보관은 종료 공지만, 초안 복귀는
  예약·보관 공지만, 소프트 삭제는 초안만 허용한다. 초안 복귀 시 게시 정보와 이전 읽음
  기록을 같은 트랜잭션에서 초기화한다.
- 공지·자료 상세 응답은 `canIssuePublicShare`, `shareEnabled`만 제공한다. 토큰은 응답에
  노출하지 않으며, 각각 `POST`·`DELETE /api/internal-notices/{id}/share-link`,
  `/api/resources/{id}/share-link`에서 작성자·관리 권한을 Service가 재검증해 발급·중단한다.
  `/share/notices/{token}`, `/share/resources/{token}`은 비로그인에게 제목과 로그인 안내만
  반환하고 `no-store`, `noindex, noarchive`를 적용한다. 로그인 사용자는 원래 상세로 리다이렉트해
  기존 접근 검사를 다시 거친다.
- 로그인 성공은 저장된 동일 출처 GET 요청만 복귀 대상으로 사용한다. `/login`, `/logout`,
  비GET·외부 출처 요청은 저장하거나 복귀하지 않고 `/dashboard`로 보낸다.
- 공지 Markdown은 `notice.MarkdownRenderer`가 GFM을 렌더링하고 allowlist sanitizer를
  거친 `SafeMarkdownHtml` 값만 만든다. JSP는 전용 `<t:markdown>` 태그만 이 값을
  원문 출력할 수 있으며, 일반 JSP/JS의 HTML 출력 금지 규칙은 그대로 유지한다.
- 공지 Markdown의 내부 이미지는 `attachment://{storedFileId}`만 원문에 저장한다. 저장 시
  `InternalNoticeService`가 첨부 연결·이미지 MIME·10MiB 제한을 검증하고,
  `MarkdownRenderer`는 검증된 ID를 해당 공지의 인증 inline URL로만 바꾼다. 작성 중인
  새 이미지는 업로더 전용 임시 미리보기 URL을 사용한다. 외부 이미지는 HTTPS URL만 직접
  렌더링하며 HTTP·data URL·원시 HTML 이미지는 렌더링하지 않는다.
- 자료실은 모든 활성 멤버가 작성·열람하고, 작성자와 `ADMIN`만 수정·소프트 삭제한다.
  자료 Markdown은 내부 첨부 이미지 참조만 렌더링하며 외부 이미지·iframe은 차단한다.
  단독 HTTPS URL의 Open Graph 메타데이터는 생성·수정 시 서버가 HTTPS 공개 대상만
  SSRF 방어 규칙으로 수집해 private 파일과 함께 스냅샷한다. 열람 화면은 이 스냅샷만
  전송하며 외부 URL을 직접 임베드하지 않는다.
- 캘린더 API는 KST `LocalDateTime`과 기존 `/api/calendar-events` 계약을 유지한다.
  일정 기간은 `endDttm > startDttm`이며, 조회 범위와의 겹침은 반개구간
  `startDttm < rangeEnd && endDttm > rangeStart`로 판정한다. 종일 일정의 종료값은
  사용자가 선택한 마지막 날 다음 날 00:00이다. `colorCode`는 제한된 일정 표시 팔레트
  (`NAVY`, `MINT`, `BLUE`, `PLUM`, `AMBER`, `ROSE`)이며, 생략한 이전 API 클라이언트는
  생성 시 `NAVY`, 수정 시 기존 값을 사용한다.
- `/activity`, `/activity/review`, `/activity/archive`와 각각의 활동 기록 API는 내 기록,
  팀장·관리자 2단계 검수, 최종 승인 아카이브를 분리한다. 팀장은 자기 팀의 1차 승인만,
  관리자는 최종 승인·긴급 승인·CSV 내보내기를 수행한다. 팀장만 작성자 자기 1차 검수를
  차단하며, 관리자는 본인 기록도 최종 검수·보관할 수 있다.
- `/activity-documents`와 `/api/activity-report-documents/**`는 모든 인증 멤버에게
  활동 내역서 HWPX 임시 저장·수정·검수 요청과 빈 양식 다운로드를 제공한다. 입력 JSON의
  활동 기록 제목은 `activity_record.title`에 저장하고, 재생성용 대표자·장소·참여자 값은
  문서 테이블에 저장한다. 정규화한 사진과 생성 HWPX는 `FileService`를 통해
  private 파일로 보관한 뒤 `activity_record_file`의 `EVIDENCE`, `DOCUMENT` 역할로 연결한다.
- 임시 저장은 `activity_record`의 `DRAFT` 상태를 만들며 검수 요청은 `SUBMITTED` 전이를
  사용한다. 팀장 승인(`TEAM_APPROVED`) 뒤에 관리자 최종 승인(`APPROVED`)을 거치고,
  긴급 최종 승인은 사유를 `activity_review_history.comment`에 남긴다. 수정 가능한 상태에서는
  HWPX와 선택적으로 사진을 교체한다.
- HWPX 엔진은 개인정보가 제거된 런타임 정본을 복사하고 namespace-aware XML DOM으로
  이름이 지정된 표 셀만 수정한다. 외부 DTD·스키마·엔티티는 비활성화하고, 사용자 문자열은
  text node로 삽입한다. 사진은 JPEG·PNG만 허용하며 EXIF 방향 적용과 메타데이터 제거 후
  1600×1200 흰 캔버스 안에 비율을 유지해 배치한다.
- 현재 회장 이름은 `activity`가 `MemberService`를 통해 조회한다. 회장 미설정 또는 비활성
  상태에서는 빈 양식과 완성본을 모두 409로 차단한다.
- `/props`와 `/api/assets`는 품목명·분류·상태·관리 방식·삭제 여부 조건을 받는 20건 단위
  `PageResponse` 목록을 제공한다. 삭제됨 조회와 등록·수정·삭제·복구는 `ADMIN`만 수행하고,
  일반 로그인 멤버는 현재 품목과 사진을 조회한다. 삭제는 `asset_item.deleted_dttm`을 기록하고
  `asset_history`와 `audit_log`에 남기며, 복구는 삭제 시각을 해제한다.
- `/props/new`, `/props/{id}`, `/props/{id}/edit`는 각각 등록·상세·수정 전용 SSR 페이지다.
  품목 사진은 한 장이며 `FileService`가 private `GENERAL` 이미지인지와 업로더 소유권을
  검증한다. 새 사진을 먼저 저장·검증한 뒤 사진 연결을 바꾸며, 새 사진을 지정하지 않은 수정은
  기존 연결을 유지한다.

## 5. 후속 범위

온보딩은 별도 승인 전까지 설계 문서만 유지하며, 현재 구현에는 포함하지 않는다.
