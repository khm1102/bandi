# AGENTS.md — bandi 에이전트 공통 규약

> 이 파일이 에이전트 규약의 원본(source of truth)이다. Claude Code는 CLAUDE.md에서 이 파일을 import하고, Codex는 이 파일을 직접 읽는다.
> 상세 규칙은 `docs/coding-convention.md`(이하 "컨벤션")를 따른다. 이 파일과 컨벤션이 충돌하면 컨벤션이 우선하고, 충돌을 발견하면 작업을 멈추고 보고한다.

## 프로젝트

- bandi (SSR 웹) — 서비스 기획/기능 정의는 별도 문서에서 관리한다 (작성 예정)
- 스택: Spring Boot 3.5.x / Java 17 / MyBatis / MySQL 8 / JSP(JSTL) / Flyway / spring-session-jdbc — **war 패키징** (JSP는 실행형 jar 미지원)
- 인증: **세션 기반** (JWT 아님 — 확정 사항)
- 베이스 패키지: `kr.ac.tukorea.bandi`, package-by-feature (`domain.{feature}`)
- 로컬 MySQL: Docker, **호스트 포트 3307** (3306 아님 — 타 프로젝트 점유). 계정 `bandi`/`bandi1234`, 스키마 `bandi`(개발)·`bandi_test`(테스트)
- Swagger UI: `http://localhost:8080/docs`, OpenAPI JSON: `/api-docs` (prod 비활성)

## 명령어

```bash
docker compose up -d        # 로컬 MySQL (선행 필수 — build/test도 DB가 떠 있어야 통과)
./gradlew build             # 빌드 + 전체 테스트 — 커밋 전 필수 게이트
./gradlew test              # 테스트만
./gradlew test --tests '*.MemberServiceTest'    # 단일 클래스 (TDD Red/Green 확인)
./gradlew test --tests '*.MemberServiceTest.이메일이_중복되면_예외가_발생한다'    # 단일 메소드
./gradlew bootRun           # dev 프로파일(기본) 실행 (Flyway 자동 적용 — .env 필요, 최초 1회 cp .env.example .env)

# 마이그레이션 로컬 적용 확인 (bootRun 1회 기동 후)
docker compose exec mysql mysql -ubandi -pbandi1234 bandi \
  -e "SELECT version, script, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5"
```

`Connection refused`로 build/test가 실패하면 `docker compose up -d` 먼저.

## 컨벤션 색인 — 작업 유형별로 해당 장만 읽는다

컨벤션(1300줄 이상)을 통째로 읽지 않는다. **0장(규칙 등급) + 작업에 해당하는 장**만 읽는다:

| 작업 | 장 |
|------|----|
| 네이밍/선언/포맷/공백 | 1~5장 |
| 클린코드/getter·setter/record | 6~7장 |
| 패키지 구조/레이어/예외 처리 | 8~9장 |
| Mapper 인터페이스/XML | 10장 + 15장(동사 사전) |
| DB 스키마/Flyway 마이그레이션 | 11장 |
| JSP 뷰 | 12장 |
| CSS/JS | 13장 |
| 테스트 | 14장 + 22.6(TDD) |
| 도구 설정(.editorconfig/IntelliJ) | 16장 |
| 설정/프로파일/.env | 17장 |
| 인증/인가/CSRF/세션 | 18장 |
| API 문서화(springdoc) | 19장 |
| 로깅 | 20장 |
| 브랜치/커밋/PR | 21장 |
| 에이전트 절차 | 22장 |

## 아키텍처 (요약 — 상세: 컨벤션 8~9장)

```
Controller → Service → Mapper → model
   (dto만)     (dto↔model 변환)
feature 간 참조는 Service → 다른 feature의 Service 만 허용
```

금지 방향: Controller→Mapper, Controller→model, Mapper→Service, model→상위 계층, feature의 mapper/model→다른 feature

## MUST 핵심 규칙 (위반 시 PR 반려)

1. 소프트탭 4칸, K&R 중괄호, 조건/반복문 중괄호 필수, 한 줄 한 문장
2. DI는 생성자 주입만 (`private final` + `@RequiredArgsConstructor`). `@Autowired` 필드 주입 금지
3. `@Data` 금지. setter는 요청 DTO에만 (예외: 9.3 분리 후 Persistence 객체의 resultMap 문제 시 — 6.6). 꺼낸 값으로 외부 분기 금지 — 판단 로직은 model의 메소드로
4. 폼 바인딩 객체는 **record 금지** (setter 클래스). 응답 DTO/Condition/Param/값 객체는 record 기본 — 단 `<form:form>`으로 폼에 바인딩되는 Condition은 setter 클래스(7.2)
5. MyBatis XML에서 **`${}` 금지** (`#{}`만). 동적 정렬은 `<choose>` 화이트리스트
6. 소프트 삭제 테이블 조회에 `deleted_dttm IS NULL` 필수 (공통 `<sql id="notDeleted">` 사용)
7. Flyway: `V{yyyyMMddHHmm}__{설명}.sql`, **적용된 파일 수정 절대 금지**, 마이그레이션과 사용 코드는 같은 커밋
8. DB: 테이블/컬럼 소문자 snake_case, PK는 `{table}_id`, MySQL ENUM 타입 금지 (예외: 프레임워크 강제 스키마 — 11.1)
9. JSP: 스크립틀릿(`<% %>`) 금지, 사용자 데이터 출력은 `<c:out>` 필수(EL `${}`는 자동 이스케이프 없음), `escapeXml="false"` 금지, 링크는 `<c:url>`, 폼은 `<form:form>`+`<form:input path>`, 검증 메시지는 `messages.properties` 키
10. CSRF 비활성화 금지 (`/api` 포함). JS 상태 변경 요청은 `js/common/api.js` fetch 래퍼만 사용
11. JS: `innerHTML`에 사용자 데이터 금지(textContent), 인라인 `onclick` 금지(data-action), `===`만
12. 예외: 커스텀 예외는 `domain.{feature}.exception`, ErrorCode 경유. 예외 삼키기·중복 로깅·`printStackTrace()` 금지
13. 로그: `@Slf4j` + 플레이스홀더. 개발 진단은 `debug`, 비즈니스 이벤트만 `info`. `System.out`·"여기 옴" 커밋 금지
14. Mapper 메소드 동사: `search`(복수)/`lookup`(단수, Optional)/`exists`/`insert`/`update`/`delete`(소프트)/`remove`(하드)

## TDD (컨벤션 22.6 — 요약)

1. 구현 전 **테스트 케이스 목록**(정상/경계/예외)을 먼저 제시한다
2. Red: 실패 테스트 작성 → 실행해서 실패 확인 (처음부터 통과하면 테스트를 의심)
3. Green: 통과시키는 최소 구현 → 4. Refactor: 테스트 수정 없이 정리
- 적용: model/Service **필수**. Mapper(`@MybatisTest`)/Controller(`@WebMvcTest`)는 구현 후 테스트 허용
- Mapper 테스트는 3종 세트 필수: `@MybatisTest` + `@AutoConfigureTestDatabase(replace = Replace.NONE)` + `@ActiveProfiles("test")` (14장 — 누락 시 실패하거나 개발 스키마 오염)
- Red 상태 커밋 금지 (테스트+구현 같은 커밋)
- `fix:`는 반드시 재현 실패 테스트 추가 → 수정 순서
- 통과를 위한 단언 완화·삭제·`@Disabled` 금지. 테스트가 요구사항과 다르면 수정하지 말고 **보고**

## 작업 완료 정의 (DoD) — 아래를 모두 만족해야 완료다

1. `./gradlew build` 통과 (Docker MySQL 기동 상태에서)
2. 구현 전 제시한 테스트 목록의 모든 케이스가 실제 테스트 메소드로 존재
3. 마이그레이션을 추가했다면 로컬 Flyway 적용 확인 (명령어 섹션의 `flyway_schema_history` 조회)
4. 새 화면을 만들었다면 해당 URL 렌더링 확인 (최소 `@WebMvcTest` 컨트롤러 테스트)
5. MUST 1~14·금지 목록 위반 없음
6. 브랜치 push + PR 생성 (본문은 `.github/PULL_REQUEST_TEMPLATE.md` 형식, 테스트 목록 포함)

## 금지 목록 (절대 — 컨벤션 22.3 + 22.5)

- 적용된 Flyway 마이그레이션 파일 수정 (새 버전 파일만). **'적용됨' 판정**: `git fetch origin` 후 `git log origin/dev -- <파일>` 결과가 있으면 적용된 것 — 수정·리네임 허용은 "내가 이 브랜치에서 추가했고 아직 dev 미머지"인 파일뿐. origin/dev가 없으면 현재 브랜치 밖에 존재한 적 있는 파일은 모두 적용된 것으로 간주 (11.4)
- `application-prod.yaml`, `SecurityConfig` 인가 규칙, `.github/` 워크플로우 무단 변경
- 비밀값(.env, 실 비밀번호/키) 생성·커밋. 새 환경변수는 `.env.example`에 키 추가
- `build.gradle.kts`/`settings.gradle.kts` 의존성·플러그인 추가/변경 (사람 승인 필요 — 필요하면 보고 후 대기)
- `git push --force`, master/dev 직접 push
- 테스트를 통과시키기 위한 테스트 삭제·주석 처리·`@Disabled` (테스트가 요구사항과 다르면 보고)
- 공유 자원(`global.**`, `common.css`, `layout.css`, `WEB-INF/tags/` 태그 파일, `messages.properties`) 수정이 필요한 작업은 시작 전 보고 — 병렬 수정 금지 대상 (22.5)

## Git

- 브랜치: `feature/{이슈번호}-{케밥설명}` (dev에서 분기), 버그는 `fix/...`
- 커밋: Conventional Commits — `feat|fix|refactor|style|docs|test|chore|db(scope): 한글 제목`
- 작업 시작 = 이슈 assign + 브랜치 push 선행 (브랜치 목록이 잠금 현황판)
- 에이전트 작업 1개 = 브랜치 1개 = PR 1개. 모든 커밋은 `./gradlew build` 통과 상태

## 검증 에이전트(Codex 등 리뷰 역할) 지침

PR 검증 시 아래 순서로 점검하고, 위반은 **컨벤션 조항 번호를 인용**해 지적한다. 조항 확인은 위 색인으로 해당 장만 읽는다:

1. **금지 목록 위반** (위 절대 금지 항목) — 발견 즉시 반려 의견
2. **의존성 방향** — import 문 기준으로 금지 방향(위 표) 검사
3. **MUST 규칙** 1~14 위반
4. **TDD 흔적** — PR의 테스트 목록과 테스트 코드가 대응하는가, 테스트 없는 model/Service 변경이 있는가, 단언이 완화·삭제된 테스트가 있는가
5. **마이그레이션** — 기존 파일 diff 여부(수정 금지), 파일명 형식, 사용 코드와 동일 PR인지
6. **보안** — CSRF 예외 추가, `<c:out>` 없는 사용자 데이터 EL 출력, `escapeXml="false"`, 스크립틀릿, MyBatis `${}`, innerHTML, 비밀값 하드코딩, 로그의 민감정보
7. SHOULD 위반은 반려가 아닌 제안(suggestion)으로만 코멘트

거짓 양성보다 누락이 낫다 — 확신 없는 지적은 "확인 요청"으로 표기한다.

## 참조 문서

- `docs/coding-convention.md` — 전체 컨벤션 (조항 번호의 출처 — 위 색인으로 필요한 장만 읽는다)
- `docs/design-guide.md` — 디자인 시스템 정본 (색상 토큰 값·타이포·셸 3종·컴포넌트 태그 명세·유틸리티 레시피). 화면 작업 전 필독, 데모는 dev `/style-guide`
- 스키마/기능 정의 문서 — **작성 예정.** 문서가 생기기 전까지는 배정된 이슈 내용만을 근거로 구현하고, 이슈에 없는 테이블·기능을 임의로 만들지 않는다. 근거가 부족하면 질문하고 대기한다
