# bandi 백엔드 설계

> 문서 상태: 현행 구현 기준
> 기준 문서: `feature-spec.md`, `database-schema.md`, `coding-convention.md`
> 작성 기준일: 2026-07-22

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
| `member` | 멤버 사전 등록, 학교 SSO 연결, 내 프로필, 팀·기수·역할·상태와 이력 |
| `calendar` | 전체·팀 일정 관리 |
| `notice` | 내부 공지, 읽음 상태 및 첨부 연결 |
| `resource` | 전체·팀 자료와 파일 리비전 |
| `activity` | 팀 활동 기록, 증빙 사진, 검토와 수정 이력 |
| `asset` | 소품·장비 품목, 개별 장비, 상태 이력과 사진 연결 |
| `file` | 파일 메타데이터와 로컬 파일 저장·전송, 프로필 사진 파기 재시도 |
| `dashboard` | 각 feature의 읽기 전용 요약 |
| `audit` | 주요 운영 변경 감사 기록 |

## 4. HTTP 경계

- 페이지 조회는 JSP SSR Controller를 사용한다.
- 부분 갱신과 상태 변경은 `/api/**`에서 세션 인증과 CSRF 보호를 적용한다.
- Swagger 계약은 `global.swagger` 인터페이스에만 둔다.
- 파일은 Spring Boot가 권한을 확인한 뒤 직접 스트리밍한다.
- `/profile`과 `/api/members/me/**`는 로그인 멤버가 사용한다. `/team-members`와
  `/api/members/team-members`는 `LEADER`·`ADMIN`만 URL 단계에서 허용하고,
  팀·대상 범위는 `MemberService`가 다시 검사한다.
- `/notices`는 인증 멤버의 내부 공지 목록·상세 화면이며, `/notices/write`와
  `/notices/{id}/edit`는 `LEADER`·`ADMIN`만 URL 단계에서 허용한다. 실제 공지 대상
  범위와 상태 전이는 `InternalNoticeService`가 다시 검사한다.
- 공지 Markdown은 `notice.MarkdownRenderer`가 GFM을 렌더링하고 allowlist sanitizer를
  거친 `SafeMarkdownHtml` 값만 만든다. JSP는 전용 `<t:markdown>` 태그만 이 값을
  원문 출력할 수 있으며, 일반 JSP/JS의 HTML 출력 금지 규칙은 그대로 유지한다.
- 공지 Markdown의 내부 이미지는 `attachment://{storedFileId}`만 원문에 저장한다. 저장 시
  `InternalNoticeService`가 첨부 연결·이미지 MIME·10MiB 제한을 검증하고,
  `MarkdownRenderer`는 검증된 ID를 해당 공지의 인증 inline URL로만 바꾼다. 작성 중인
  새 이미지는 업로더 전용 임시 미리보기 URL을 사용한다. 외부 이미지는 HTTPS URL만 직접
  렌더링하며 HTTP·data URL·원시 HTML 이미지는 렌더링하지 않는다.
- 캘린더 API는 KST `LocalDateTime`과 기존 `/api/calendar-events` 계약을 유지한다.
  일정 기간은 `endDttm > startDttm`이며, 조회 범위와의 겹침은 반개구간
  `startDttm < rangeEnd && endDttm > rangeStart`로 판정한다. 종일 일정의 종료값은
  사용자가 선택한 마지막 날 다음 날 00:00이다. `colorCode`는 제한된 일정 표시 팔레트
  (`NAVY`, `MINT`, `BLUE`, `PLUM`, `AMBER`, `ROSE`)이며, 생략한 이전 API 클라이언트는
  생성 시 `NAVY`, 수정 시 기존 값을 사용한다.

## 5. 후속 범위

온보딩은 별도 승인 전까지 설계 문서만 유지하며, 현재 구현에는 포함하지 않는다.
