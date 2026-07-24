# bandi

연극 동아리 통합 관리 시스템 (SSR 웹). 단원·일정·공지·자료·활동 기록·소품과
권한을 관리한다. 1차 기능과 스키마 기준선은 확정됐으며 온보딩은 후속 범위다.

## 스택

| 구분 | 선택 | 비고 |
|---|---|---|
| 백엔드 | Spring Boot 3.5.x · Java 17 | **war 패키징** (JSP는 실행형 jar 미지원) |
| 뷰 | **JSP + JSTL + 태그 파일** | Thymeleaf에서 전환 (2026-07) — 컨벤션 12장 |
| 스타일 | Tailwind CSS v4 (Play CDN) + shadcn 토큰 | 화이트/네이비/버건디 팔레트 — `docs/design-guide.md` |
| DB | MySQL 8.4 (Docker) · MyBatis · Flyway | 호스트 포트 **3307** |
| 파일 저장 | 로컬 영구 볼륨 | `FILE_STORAGE_ROOT` 아래 private/public 분리 |
| 인증 | 세션 기반 (spring-session-jdbc) | JWT 아님 — 확정 사항 |

## 시작하기

```bash
cp .env.example .env        # 최초 1회 — 로컬은 팀 공통 더미값 그대로 사용
docker compose up -d        # MySQL 8.4 (3307, bandi/bandi1234, 스키마 bandi·bandi_test)
./gradlew bootRun           # dev 프로파일(기본), Flyway 자동 적용
```

| URL | 설명 |
|---|---|
| `http://localhost:8080/docs` | Swagger UI (prod 비활성) |

빌드/테스트: `./gradlew build` (Docker MySQL이 떠 있어야 통과 — 커밋 전 필수 게이트)

## 프로파일

`dev`(로컬 개발 기본, 미지정 시 자동) / `prod` 2단계 + 테스트 전용 `test`. DB 접속값은 `.env`에서 로딩한다(`DB_HOST`/`DB_PORT`/`DB_USERNAME`/`DB_PASSWORD`). 상세: 컨벤션 17장.

파일 저장은 `FILE_STORAGE_ROOT` 아래의 로컬 영구 볼륨을 사용한다. Spring Boot가
권한을 확인한 뒤 파일을 직접 스트리밍하며, MinIO·S3 호환 저장소·presigned URL은
사용하지 않는다. 운영 기본 경로는 `/data/bandi`다.

## 팀 공유 테스트 서버

중앙 Cloudflare Tunnel 뒤에서 동작하는 `app + MySQL` 테스트 서버 구성은
[docs/test-server-deployment.md](docs/test-server-deployment.md)를 따른다. Tunnel 토큰과
서버 환경값은 이 저장소에 넣지 않는다.

## 현재 상태 / 임시 결정 (로그인 기능 도입 시 해소)

- **Security는 임시 전체 개방**(`SecurityConfig`의 `permitAll`) — 로그인 기능이 없어서다. CSRF는 활성 유지. 로그인 구현 시 인가 규칙을 작성한다 (컨벤션 18장이 목표 상태)
- 기능은 `docs/feature-spec.md`, 스키마는 `docs/database-schema.md`를 구현 기준으로 사용한다
- 컨벤션 18.2의 역할 코드와 11.3의 `CHECK`·generated column 규칙은 확정 스키마에 맞춰 동기화돼 있다
- 온보딩 화면·API·테이블은 후속 범위이며 1차 구현에 포함하지 않는다

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
| [docs/feature-spec.md](docs/feature-spec.md) | 1차 기능 범위와 구현 순서 정본 |
| [docs/database-schema.md](docs/database-schema.md) | 테이블·제약·트랜잭션·마이그레이션 순서 정본 |
| [docs/member-onboarding-plan.md](docs/member-onboarding-plan.md) | 후속 온보딩 설계 기록 (1차 구현 제외) |
| [AGENTS.md](AGENTS.md) | AI 에이전트 공통 규약 (MUST 규칙·TDD·DoD·금지 목록) |

## Git

- 브랜치 `feature/{이슈번호}-{케밥설명}` (dev에서 분기) · 커밋은 Conventional Commits 한글 제목 · master/dev 직접 push 금지
- 작업 1개 = 브랜치 1개 = PR 1개, 모든 커밋은 `./gradlew build` 통과 상태 (상세: 컨벤션 21장)
