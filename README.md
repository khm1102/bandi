# bandi

연극 동아리 통합 관리 시스템 (SSR 웹). 단원·공연·회비·일정 관리와 공개 예매 화면을 목표로 한다 — 기능 정의는 확정 중이며, 디자인 방향과 개발 기반이 먼저 확정된 상태다.

## 스택

| 구분 | 선택 | 비고 |
|---|---|---|
| 백엔드 | Spring Boot 3.5.x · Java 17 | **war 패키징** (JSP는 실행형 jar 미지원) |
| 뷰 | **JSP + JSTL + 태그 파일** | Thymeleaf에서 전환 (2026-07) — 컨벤션 12장 |
| 스타일 | Tailwind CSS v4 (Play CDN) + shadcn 토큰 | 화이트/네이비/민트 팔레트 — `docs/design-guide.md` |
| DB | MySQL 8.4 (Docker) · MyBatis · Flyway | 호스트 포트 **3307** |
| 인증 | 세션 기반 (spring-session-jdbc) | JWT 아님 — 확정 사항 |

## 시작하기

```bash
cp .env.example .env        # 최초 1회 — 로컬은 팀 공통 더미값 그대로 사용
docker compose up -d        # MySQL 8.4 (3307, bandi/bandi1234, 스키마 bandi·bandi_test)
./gradlew bootRun           # dev 프로파일(기본), Flyway 자동 적용
```

| URL | 설명 |
|---|---|
| `http://localhost:8080/style-guide` | 디자인 시스템 데모 (dev 전용) — 화면 작업 시 여기서 복사 |
| `http://localhost:8080/docs` | Swagger UI (prod 비활성) |

빌드/테스트: `./gradlew build` (Docker MySQL이 떠 있어야 통과 — 커밋 전 필수 게이트)

## 프로파일

`dev`(로컬 개발 기본, 미지정 시 자동) / `prod` 2단계 + 테스트 전용 `test`. DB 접속값은 `.env`에서 로딩한다(`DB_HOST`/`DB_PORT`/`DB_USERNAME`/`DB_PASSWORD`). 상세: 컨벤션 17장.

## 현재 상태 / 임시 결정 (로그인 기능 도입 시 해소)

- **Security는 임시 전체 개방**(`SecurityConfig`의 `permitAll`) — 로그인 기능이 없어서다. CSRF는 활성 유지. 로그인 구현 시 인가 규칙을 작성한다 (컨벤션 18장이 목표 상태)
- 기능/스키마 정의 문서 작성 예정 — 그 전까지 이슈 내용만을 근거로 구현한다
- `/style-guide`의 사이드바 내비는 feature 확정 시 항목을 채운다 (`WEB-INF/tags/layout.tag`)

## 프로젝트 구조 (요약)

```
src/main/java/kr/ac/tukorea/bandi
├── domain/{feature}/          # package-by-feature: controller·service·mapper·model·dto·exception
└── global/                    # config·security·exception·response
src/main/webapp/WEB-INF
├── tags/                      # 레이아웃 셸 3종 + 컴포넌트 태그 (공유 자원)
└── views/{feature}/*.jsp
src/main/resources
├── db/migration/              # Flyway — V{yyyyMMddHHmm}__{설명}.sql
├── mapper/                    # MyBatis XML
└── static/css·js/
```

## 문서

| 문서 | 내용 |
|---|---|
| [docs/coding-convention.md](docs/coding-convention.md) | 전체 컨벤션 (통독 금지 — AGENTS.md의 색인으로 필요한 장만) |
| [docs/design-guide.md](docs/design-guide.md) | 디자인 시스템 정본 — 토큰·타이포·셸·컴포넌트 명세·레시피 |
| [AGENTS.md](AGENTS.md) | AI 에이전트 공통 규약 (MUST 규칙·TDD·DoD·금지 목록) |

## Git

- 브랜치 `feature/{이슈번호}-{케밥설명}` (dev에서 분기) · 커밋은 Conventional Commits 한글 제목 · master/dev 직접 push 금지
- 작업 1개 = 브랜치 1개 = PR 1개, 모든 커밋은 `./gradlew build` 통과 상태 (상세: 컨벤션 21장)
