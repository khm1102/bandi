# CLAUDE.md — bandi

@AGENTS.md

## Claude Code 전용 규칙

- **plan mode 우선**: 3개 파일 이상을 건드리는 작업은 계획을 먼저 제시하고 승인 후 진행한다
- 작업 시작 시 확인 순서: ① 이슈 내용 → ② `docs/feature-spec.md`와 `docs/database-schema.md`의 해당 범위 → ③ 건드릴 feature의 기존 코드 패턴 (기존 코드가 없으면 컨벤션 8~9장의 예시 코드가 패턴 정본). 기존 코드와 컨벤션이 다르면 컨벤션을 따르되 보고한다
- 컨벤션 상세가 필요하면 AGENTS.md의 **색인**으로 해당 장만 읽는다 (통독 금지)
- 커밋 직전 자가 점검: AGENTS.md의 **작업 완료 정의(DoD)** 체크리스트 1~5를 점검한다
- 커밋·PR 작성 시 AGENTS.md의 Git identity 규칙을 따른다. `Co-Authored-By`에 Claude를 추가하거나 `Generated with`, Claude·모델명 등 자동 생성 주체를 표기하지 않는다
- 마이그레이션 파일 생성 시 파일명 타임스탬프는 실제 현재 시각(`date +%Y%m%d%H%M`)으로 채번한다
- `src/main/resources/mapper/**` XML을 수정하면 대응하는 Mapper 인터페이스 시그니처와 일치하는지 반드시 교차 확인한다
- 요구사항이 모호하면 구현으로 때우지 말고 **질문한다**. 특히 인가 정책(누가 이 기능을 쓸 수 있는가)이 이슈에 없으면 반드시 물어본다

## 서브에이전트 운용 (컨벤션 22.5)

- 분할 단위는 feature 패키지. 서로 다른 feature만 병렬 배정
- 공유 자원(`global.**`, `common.css`, `layout.css`, `WEB-INF/tags/`, `messages.properties`)을 수정하는 작업은 서브에이전트에 위임하지 않고 메인 세션에서 직렬 처리
- 다른 feature의 Service 시그니처가 필요하면 시그니처만 먼저 정의·커밋한 후 병렬 시작

## 세션 종료 전

- AGENTS.md의 **작업 완료 정의(DoD)** 6개 항목을 모두 만족시킨다 (브랜치 push + PR 생성 포함)
- 미완료 상태로 끝나면 남은 작업을 PR 코멘트 또는 이슈 코멘트로 기록한다
