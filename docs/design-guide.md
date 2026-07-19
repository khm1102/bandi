# bandi 디자인 가이드

> 정체성: **화이트 캔버스 + 네이비 셸 + 버건디 포인트** (2026-07-20 민트 → 버건디 전환)
> 살아있는 데모: dev 프로파일(기본) `http://localhost:8080/style-guide` — 데모 페이지는 순차 갱신 중이며 값 충돌 시 이 문서가 정본이다.
> 규칙 등급·스타일링 방식은 코딩 컨벤션 13장을 따른다. 이 문서는 "무슨 값·무슨 조합·무슨 구조를 쓰는가"의 정본이다.
> 제품 원칙·화면별 spec: `.ui-craft/brief.md`, `.ui-craft/spec.md` · 마이그레이션 순서: `docs/frontend-redesign-plan.md`

## 0. 제품 설계 원칙 (모든 화면 공통)

1. **한 화면은 하나의 핵심 결정·행동을 돕는다.** 화면(뷰포트)당 primary CTA는 하나만 둔다. 나머지는 outline·텍스트 링크로 낮춘다.
2. **다음 행동을 상태보다 먼저 보여준다.** 상태 나열·통계 그리드로 화면을 시작하지 않는다. "지금 무엇을 하면 되는지"가 첫 정보다.
3. **점진 공개.** 첫 화면은 요약과 핵심 행동만, 세부는 섹션 전환·sheet·accordion·접기로 공개한다.
4. **사용자 언어로 설명한다.** 내부 코드값(`ENTRY_OPEN` 등)을 그대로 노출하지 않고 결과 중심 문구로 번역한다.
5. **중요한 작업은 토스트로 끝내지 않는다.** 결과와 다음 행동을 화면 안(result 영역, sheet 내부)에서 보여주고, 토스트는 보조 피드백으로만 쓴다.
6. **실수 복구를 우선한다.** 확인 절차를 늘리기보다 취소·정정·이력 접근을 제공한다.
7. **모바일과 데스크톱은 적응형이다.** 320px부터 기능을 보장하되, 1024px 이상은 모바일의 확대판이 아니라 비교·편집·일괄 처리를 위한 별도 작업면으로 재구성한다.

## 1. 아이덴티티

- **화이트 캔버스 + 네이비 셸 + 버건디 포인트.** 콘텐츠 영역은 밝고 조용하게, 좌측 사이드바(관리자)와 상단 내비(공개)는 딥 네이비. 행동 유도·활성 상태만 버건디(#8E0015 — 무대 커튼).
- **버건디 위 글자는 흰색이다** (`--primary-foreground: #FFFFFF`, 대비 9.73:1). hover는 `primary-strong`(#6F0011) + 흰 글자(12.47:1). 민트 시절의 "민트 위 다크 네이비" 규칙은 폐기.
- 카드는 그림자 없이 보더로 구분한다. 그림자는 부유 요소(모달·sheet·토스트)와 데스크톱 인증 셸에만 `shadow-xl`/`shadow-lg`.
- 한 화면(뷰포트)에 버건디 주요 배치는 **3~5곳 이하**: primary CTA 1 + 활성 내비 1 + 핵심 강조 1~2.

## 2. 색상 토큰 (tokens.css — 공유 자원)

| 토큰 | 값 | 용도 | 대비 근거 (WCAG) |
|---|---|---|---|
| `background` / `foreground` | `#f4f7f9` / `#102235` | 페이지 바탕 / 기본 글자 | 14.9:1 |
| `card` | `#ffffff` | 카드·패널·탑바 | — |
| `primary` / `primary-foreground` | `#8e0015` / `#ffffff` | 주 버튼, 활성 내비, 선택 상태 | 흰 글자 9.73:1 AA·AAA |
| `primary-strong` | `#6f0011` | primary hover | 흰 글자 12.47:1 |
| `secondary` / `muted` | `#f5f8fa` | 서브 배경, 테이블 헤더, hover 배경 | — |
| `muted-foreground` | `#56697c` | 보조 텍스트·라벨 | 바탕 대비 5.26:1 |
| `accent` / `accent-foreground` | `#f9eaed` / `#7a0012` | 버건디 소프트 배경 / 링크·아이콘·강조 텍스트 | soft 위 본색 9.82:1 |
| `success` / `success-soft` | `#087054` / `#e8f8f2` | 완료·납부·긍정 | 유지 |
| `warning` / `warning-soft` | `#80530a` / `#fff6e6` | 확인 필요 | 유지 |
| `destructive` / `destructive-soft` | `#b42318` / `#feeceb` | 삭제·미납·오류 | 흰 글자 6.57:1, soft 위 본색 5.77:1 |
| `info` / `info-soft` | `#315f8f` / `#edf4fb` | 공지·중립 정보 | 유지 |
| `border` / `input` / `ring` | `#dfe7ec` / `#dfe7ec` / `#8e0015` | 보더 / 입력 보더 / 포커스 링 | 링 바탕 대비 9.05:1 |
| `sidebar` 계열 | `#0b1f33` 외 | 네이비 셸 전용 | 유지 |

- 고정 primary 값(#8E0015)은 변경하지 않는다. 파생 토큰이 AA를 통과하지 못할 때만 더 어두운 값으로 조정하고 이 표에 근거를 기록한다.
- 상태 표시는 항상 **`*-soft` 배경 + 본색 글자** 조합. 본색 배경 + 흰 글자는 버튼(primary·destructive)에만.
- **primary(버건디)와 destructive(빨강)를 색상만으로 구분하지 않는다.** 위험 행동에는 경고 아이콘 + 결과 설명 + 명확한 동사 + 별도 확인 구조(6장)를 반드시 함께 쓴다.
- 팀 태그용 저채도 5색은 계속 보류. 민트 후보(`#2cc7a5`)는 버건디 전환에 따라 재선정 대상.

## 3. 셸 (레이아웃 태그 3종)

| 태그 | 용도 | 구조 |
|---|---|---|
| `<t:layout title active crumb>` | 관리자 화면 전부 | 네이비 **그룹형 사이드바**(`w-56`, lg 미만 서랍) + 모바일 상단바 + 데스크톱 `max-w-screen-2xl` 작업면 |
| `<t:layoutPublic title>` | 예매 등 공개 화면, 에러 페이지 | 네이비 상단 내비 + 데스크톱 `max-w-6xl` 본문 + footer |
| `<t:layoutAuth title>` | 로그인 | 데스크톱 분할 셸, 모바일 단일 열 |

### 3.1 그룹형 사이드바 (sidebar.tag — 공유 자원)

메뉴는 다음 그룹으로 묶는다. **역할 조건은 기존 그대로**(ADMIN 전용 항목을 다른 역할에 노출하지 않는다). URL도 변경하지 않는다.

```
홈
동아리 운영: 통합 캘린더 · 공지·자료실 · 활동 기록 · 행사·출석 · 회비 · 멤버·권한(ADMIN)
공연 제작: 팀별 제작 진행 · 소품·장비 · 체크리스트
공연 운영(ADMIN): 공연 운영 설정 · 공연 콘텐츠 · 관람 신청 관리 · 공연 당일 입장 · 공시 관리
```

- 그룹 라벨은 `text-xs font-bold text-sidebar-muted`, 장식 대문자 금지.
- 활성 항목: `bg-primary text-primary-foreground`(버건디+흰색). `aria-current="page"`.
- 모바일 서랍: 열렸을 때만 배경 스크롤 잠금·`inert` 해제, Escape 닫기, 닫으면 토글 버튼으로 포커스 복귀. JS 실패 시 데스크톱(lg 이상)에서는 CSS만으로 항상 보인다.

## 4. 타이포그래피

폰트: **Noto Sans KR**. **900(font-black)을 화면 전체에 반복하지 않는다** — 극단 굵기는 쓰지 않고 800까지만 사용한다.

| 용도 | 유틸리티 |
|---|---|
| 페이지 제목 | `text-2xl font-extrabold tracking-tight` (24px/800) |
| 섹션 제목 | `text-lg font-bold` (18px/700) |
| 카드·sheet 제목 | `text-base font-bold` |
| 본문·목록 셀 | `text-sm`(14px) ~ `text-base`(16px), 400~500 |
| 라벨·테이블 헤더 | `text-xs font-bold text-muted-foreground` (12px — 13px는 프리셋에 없어 12px 사용) |
| 통계 값·숫자 | `text-2xl font-extrabold tracking-tight tabular-nums` |

- 숫자는 항상 `tabular-nums`.
- 장문 본문 컬럼은 `max-w-prose`(≈65자)를 유지한다.

## 5. 레이아웃 · 간격 · 카드 기준

- 반응형: **모바일 퍼스트**, 브레이크포인트는 `md`/`lg`만. **최소 지원 폭 320px** — 320px에서 가로 넘침이 없어야 한다.
- **lg(1024px+) 적응 규칙**: 목록은 구조화 행/표, 설정은 좌측 섹션 내비+우측 편집, 현장 운영은 입력+결과 2열, 캘린더는 월 그리드를 사용한다. 모바일 카드·accordion·단일 열은 공간이 부족할 때의 표현이지 제품 전체의 기본 표현이 아니다.
- 내부 본문은 `max-w-screen-2xl`(1536px)까지 사용한다. 장문 읽기 영역만 `max-w-prose`/`max-w-3xl`로 제한하며, 페이지 전체에 `max-w-2xl`~`max-w-5xl`을 중복 적용하지 않는다.
- 데스크톱에서는 한 뷰포트에 목록과 상세/보조 작업을 함께 둘 수 있다. 단, 같은 데이터를 중복 표시하거나 primary CTA를 늘리지는 않는다.
- 모든 클릭·터치 대상 최소 **44×44px**(`min-h-11`). 데스크톱 전용 툴바만 `md:min-h-9` 축소 허용.
- **모든 영역을 카드로 감싸지 않는다.** 카드 사용 기준:
  - 카드 O: 컬렉션의 동질 항목(목록의 행 그룹), 명확히 독립된 부유 요소
  - 카드 X: 페이지의 주요 섹션 구분 — 여백(`space-y-8`), 섹션 제목, 구분선(`border-t`), 배경 차이(`bg-secondary`)로 관계를 표현한다
- 그림자는 sheet·modal·toast 등 실제 부유 요소에만.
- 그라디언트, 장식용 원형 그래픽, glassmorphism, 과한 배지·상태색 금지.
- 상태는 **색상 + 아이콘 + 텍스트**를 함께 쓴다. 색상 단독 표시는 금지.

### 5.1 목록 패턴 (action-heavy vs 읽기 중심)

- **action-heavy 목록**(행마다 조치가 있는 운영 목록 — 신청 관리, 멤버, 출석): 모바일은 세로 카드/리스트, 데스크톱은 구조화 목록(그리드 행). **모바일 가로 스크롤 테이블 금지.** 행 전체 또는 "열기" 버튼이 상세 sheet를 연다.
- **`<t:dataTable>`은 읽기 중심의 고밀도 표에만** 사용한다(변경 이력, 통계 명세 등). 이때만 가로 스크롤 허용.

### 5.2 sheet vs 페이지 섹션 vs modal

- **sheet**: 짧은 생성·수정 폼(입력 ~7개 이하), 목록 항목의 상세·조치. 모바일 하단, 데스크톱 우측에서 열린다.
- **페이지 내부 섹션**: 장문·복잡한 설정(외부 공개 폼, 관람 안내). 섹션 전환은 URL hash로 보존.
- **modal**: 파괴적 동작의 최종 확인(공용 confirm) 등 초단문. 중첩 modal은 만들지 않는다.

### 5.3 sticky action bar

- 장문 폼(외부 공개·관람 안내)은 하단 sticky action bar에 저장 버튼과 저장하지 않은 변경 표시("저장하지 않은 변경이 있어요")를 둔다.
- 모바일에서 `pb-[env(safe-area-inset-bottom)]` 상당의 안전 영역을 확보하고, 키보드 표시 상태에서도 primary action에 접근할 수 있어야 한다.
- 저장 중에는 버튼을 잠그고 진행 상태를 표시한다(중복 제출 방지).

## 6. 위험 작업 확인 패턴

위험 행동(취소·삭제·입장 취소 등)은 다음을 **모두** 갖춘다:

1. 경고 아이콘 + `destructive` 계열 시각 (색상 단독 금지)
2. **대상·현재 상태·실행 후 결과**를 먼저 설명 ("A1·A2 좌석 2석의 신청을 취소해요. 취소한 좌석은 다시 신청할 수 있게 돌아가요.")
3. 결과를 말하는 동사 버튼 ("신청 취소", "입장 취소" — "확인"·"삭제합니다" 금지)
4. 필요 시 사유 입력 (운영 기록 명시: "사유는 운영 기록에 남아요.")
5. 실행 후 복구 경로 안내 또는 이력 접근

## 7. 상태 계약 (모든 화면 공통)

| 상태 | 계약 |
|---|---|
| loading | 최종 레이아웃과 같은 형태의 placeholder(스켈레톤 또는 안정된 문구 블록). 레이아웃 이동 금지 |
| empty | 비어 있는 **이유** + 시작 행동. 빈 표·"데이터 없음" 단독 금지 |
| error | 실패 **원인** + 현재 입력 보존 여부 + 재시도 행동 |
| partial | 일부 영역 실패 시 성공 데이터는 유지, 실패 영역만 인라인 오류 + 재시도 |
| success | 결과 + 다음 행동을 화면 안에서. 토스트는 보조 |
| conflict | "다른 곳에서 변경됐어요" + 새로고침·재조회 행동 |
| unauthorized | 입력을 지우거나 즉시 리다이렉트하지 않는다. 재로그인 필요와 작성 데이터 보존 상태를 알린다 |

## 8. UX Writing (해요체)

- 친근하고 직접적인 **해요체**: "외부 공연 페이지를 저장했어요.", "입장할 좌석을 선택해 주세요."
- 버튼은 결과를 말한다: "신청 취소", "선택 좌석 입장", "첫 회차 만들기". "확인"·"처리"·"제출" 같은 중립 단어 금지.
- 오류 문구 3요소: 원인 + 데이터 보존 여부 + 복구 행동.
- 영문 대문자 eyebrow·장식 문구·이모지 아이콘 금지.
- 검증 메시지는 `messages.properties` 키 기준 문구와 일치시킨다.

## 9. 모션 · 접근성

- 모션은 **100~200ms opacity/transform만**. `transition-all` 금지 — 속성을 명시한다(`transition-opacity`, `transition-transform`, `transition-colors`).
- **`prefers-reduced-motion` 전역 지원**(head.tag의 `@layer base`) — 감축 설정에서는 전환·애니메이션을 즉시 완료로 처리한다.
- sheet/modal: 포커스 트랩, Escape 닫기, 닫은 후 트리거로 포커스 복귀, 배경 스크롤 잠금, `aria-modal`.
- 여러 겹이 열리면 **최상단 레이어만** Escape·Tab을 처리한다.
- `aria-live`는 짧은 상태 문구에만. 목록·달력 전체에 걸지 않는다.
- 입력 오류는 `aria-invalid` + `aria-describedby`로 연결한다.
- `focus-visible` 링은 전역 기본(head.tag) 유지.

## 10. 컴포넌트 태그 (attribute 명세)

| 태그 | attributes | 비고 |
|---|---|---|
| `<t:pageHead>` | `title`*, `description`, body=우측 액션 | 페이지 최상단 1회. primary CTA는 화면당 하나만 |
| `<t:card>` | `title`, `moreUrl`, `moreLabel`, `flush` | 컬렉션·독립 블록에만 (5장 기준) |
| `<t:statCard>` | 기존 동일 | **화면 상단 4연속 통계 그리드 금지** — compact summary(레시피 11.2)를 우선 |
| `<t:badge>` | `tone`*, `dot`, body | 상태 표시 — 아이콘·문구 병행 |
| `<t:emptyState>` | `title`*, `message`, body | 이유 + 시작 행동 포함 |
| `<t:modal>` | `id`*, `title`*, `description`, `footer` | 확인 등 초단문 전용 |
| `<t:sheet>` | `id`*, `title`*, `description`, `footer`(fragment), body | 모바일 하단·데스크톱 우측 패널. `openSheet(id)`/`data-open-sheet`. 포커스 트랩·Esc·복귀·스크롤 잠금 내장 |
| `<t:formField>` | 기존 동일 | `<form:form>` 내부 전용 |
| `<t:button>` | 기존 동일 | 기본 높이 44px |
| `<t:dataTable>` | `caption`*, `cssClass` | **읽기 중심 고밀도 표 전용** (5.1) |
| `<t:filterChip>` | 기존 동일 | 활성 상태는 네이비 유지 |

### 10.1 상호작용 규약 (공통 JS)

| 규약 | 사용법 |
|---|---|
| 토스트 | `showToast(message)` — **보조 피드백 전용.** 결과는 화면 안에 먼저 표시 |
| flash 메시지 | 기존 동일 (redirect 흐름) |
| 모달 | `openModal(id)` / `data-open-modal` — 숨겨진 요소는 포커스 대상에서 제외, 최상단 레이어만 키 처리 |
| sheet | `openSheet(id)` / `data-open-sheet` — 모달과 동일한 키보드 계약 |
| confirm | `data-confirm` — 6장 위험 작업 패턴과 함께 사용 |
| 중복 제출 방지 | `data-guard` 또는 페이지 JS의 busy 잠금 |

## 11. 레시피

### 11.1 기본 (버튼·입력·칩 — 기존 유지, 색만 토큰 따라 변경)

**버튼** — base: `inline-flex min-h-11 items-center justify-center rounded-md px-4 text-sm font-bold transition-colors disabled:pointer-events-none disabled:opacity-50`

| variant | 추가 클래스 |
|---|---|
| primary | `bg-primary text-primary-foreground hover:bg-primary-strong focus-visible:ring-2 focus-visible:ring-ring` |
| outline | `border bg-card hover:bg-secondary` |
| dark(네이비) | `bg-sidebar text-white hover:bg-sidebar-accent` |
| danger | `bg-destructive text-destructive-foreground hover:bg-destructive/90` + 경고 아이콘 병행 |

**입력** — `h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm` (모바일 16px로 iOS 확대 방지)

### 11.2 compact summary (통계 그리드 대체)

한 줄 요약: `flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-muted-foreground` + 값은 `font-bold text-foreground tabular-nums`.
예: "신청 **12건** · 유효 좌석 **31석** · 입장 **8석** (26%)". 4칸 statCard 그리드는 대시보드성 화면에서만.

### 11.3 준비 체크리스트 (설정 화면)

- 항목: `flex min-h-11 items-center gap-3` + 상태 아이콘(완료 ✓ success / 다음 → primary / 대기 ○ muted) + 라벨 + 이동 링크
- "다음" 항목 하나만 버건디 강조. 완료 항목은 조용하게.

### 11.4 목록 행 (action-heavy, 모바일)

- 행: `w-full rounded-lg border bg-card p-4 text-left` (button 요소) — 탭하면 상세 sheet
- 1행: 식별자(`font-mono text-xs`) + 이름(`font-bold`) / 2행: 좌석·부가 정보 + 상태 배지
- 데스크톱(md+): `grid grid-cols-[...] items-center` 구조화 행으로 전환

### 11.5 sticky action bar

`sticky bottom-0 z-10 -mx-4 border-t bg-card/95 px-4 py-3 backdrop-blur md:static md:mx-0 md:border-0 md:bg-transparent md:px-0`
+ 좌측 변경 상태 문구(`aria-live="polite"`), 우측 저장 버튼.

### 11.6 result feedback 영역 (현장 운영)

- 컨테이너: `rounded-lg border p-4` + 상태별 soft 배경(`bg-success-soft` 등) + 리드 아이콘 + 제목 + 설명 + 다음 행동 버튼
- `role="status"` 또는 제목에 포커스 이동. 토스트 병행은 선택.

### 11.7 캘린더·시간 그리드 / 인증 / 예매·좌석맵

기존 레시피 유지하되 색상은 새 토큰을 따른다(민트 언급은 모두 버건디 계열로 대체). 세부 값은 이전 판 레시피를 승계하며 해당 기능 재설계 단계(frontend-redesign-plan 3장)에서 갱신한다.

## 12. 금지/주의

- 팔레트 유틸리티(`bg-red-500`)·임의 색값 금지 — 새 색은 tokens.css + head.tag 매핑에 함께 추가
- 상태색 본색을 넓은 면적 배경으로 쓰지 않는다 (soft 배경 + 본색 글자)
- 카드에 그림자 금지, `rounded-2xl` 이상 금지
- `border-l-4` 등 굵은 색상선 장식 금지
- 가로 스크롤은 읽기 중심 표에만. 모바일 주 내비·action-heavy 목록에 금지
- `transition-all` 금지, 900 굵기 반복 금지, 영문 대문자 eyebrow 금지
- 한 화면 primary CTA 2개 이상 금지, 버건디 주요 배치 5곳 초과 금지
- QR 토큰·개인정보를 URL·로그·DOM에 잔류시키지 않는다 (showops spec 참조)

## 13. 아이콘 · 포맷 (기존 유지)

- **Lucide** SVG 인라인, `viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"`, 크기 `size-4`/`size-5`, 색은 `currentColor` 상속
- 장식 아이콘 `aria-hidden="true"`, 의미 아이콘은 텍스트 병기
- 날짜 `yyyy.MM.dd(E)`, 시간 `HH:mm`, 금액 `#,##0`원 (JSTL fmt)
- 테이블 모바일 전략: 읽기 중심 표만 `overflow-x-auto`, action-heavy 목록은 카드 전환(5.1)
- Tailwind Play CDN prod 유지 (버전·SRI 고정) — 재검토 조건 기존과 동일
