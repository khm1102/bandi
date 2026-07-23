# bandi 디자인 가이드

> 초기 화면 프로토타입에서 확정한 화이트/네이비/민트 방향을 유지한다
> 규칙 등급·스타일링 방식은 코딩 컨벤션 13장을 따른다. 이 문서는 "무슨 값·무슨 조합을 쓰는가"의 정본이다.

## 1. 아이덴티티

- **화이트 캔버스 + 네이비 셸 + 민트 포인트.** 콘텐츠 영역은 밝고 조용하게, 좌측 사이드바(관리자)와 상단 내비(공개)는 딥 네이비, 행동 유도와 활성 상태만 민트.
- **민트 위 글자는 흰색이 아니라 다크 네이비(`--primary-foreground: #08251f`)다.** 민트는 밝은 색이라 흰 글자는 대비가 부족하다. hover 시에는 AA 대비를 확보한 `primary-strong` + 흰 글자로 전환한다.
- 카드는 그림자 없이 보더로 구분한다. 그림자는 부유 요소(모달, 토스트)와 데스크톱 인증 셸에만 `shadow-xl`/`shadow-lg`.

## 2. 색상 토큰 (tokens.css — 공유 자원)

| 토큰 | 값 | 용도 |
|---|---|---|
| `background` / `foreground` | `#f4f7f9` / `#102235` | 페이지 바탕 / 기본 글자 |
| `card` | `#ffffff` | 카드·패널·탑바 |
| `primary` / `primary-foreground` | `#2cc7a5` / `#08251f` | 주 버튼, 활성 내비, 선택 상태 |
| `primary-strong` | `#0b715f` | primary hover (흰 글자 대비 5.93:1) |
| `secondary` / `muted` | `#f5f8fa` | 서브 배경, 테이블 헤더, hover 배경 |
| `muted-foreground` | `#56697c` | 보조 텍스트·라벨 (페이지 바탕 대비 5.26:1) |
| `accent` / `accent-foreground` | `#e9fbf6` / `#0f6f5d` | 민트 소프트 배경 / 링크·아이콘·강조 텍스트 |
| `success` / `success-soft` | `#087054` / `#e8f8f2` | 완료·납부·긍정 |
| `warning` / `warning-soft` | `#80530a` / `#fff6e6` | 확인 필요 |
| `destructive` / `destructive-soft` | `#a63c4d` / `#fff0f2` | 삭제·미납·오류 |
| `info` / `info-soft` | `#315f8f` / `#edf4fb` | 공지·중립 정보 |
| `border` / `input` / `ring` | `#dfe7ec` / `#dfe7ec` / `#2cc7a5` | 보더 / 입력 보더 / 포커스 링 |
| `sidebar` 계열 | `#0b1f33` 외 | 네이비 셸 전용 (아래 3장) |

- 상태 표시는 항상 **`*-soft` 배경 + 본색 글자** 조합 (배지·배너). 본색 배경 + 흰 글자는 버튼(destructive)에만.
- **팀 태그용 저채도 5색은 보류** — 팀 기능 확정 시 아래 후보로 토큰화한다: `#6b8499`(연출) `#527eaa`(무대) `#2cc7a5`(운영) `#b7984e`(디자인) `#7b8fa1`(영상)

## 3. 셸 (레이아웃 태그 3종)

| 태그 | 용도 | 구조 |
|---|---|---|
| `<t:layout title active crumb>` | 관리자 화면 전부 | 네이비 사이드바(`w-56`, lg 미만 접근 가능한 서랍 내비) + 스티키 탑바 + `max-w-6xl` 반응형 본문 |
| `<t:layoutPublic title>` | 오류 등 비인증 화면 | 네이비 상단 내비(header.tag) + `max-w-5xl` 본문 + footer.tag |
| `<t:layoutAuth title>` | 로그인 | 데스크톱은 네이비 동아리 소개 패널 + `max-w-md` 폼의 분할 셸, 모바일은 폼에 집중하는 단일 열 |

- 공통 `<head>`(폰트·tokens.css·Tailwind `@theme` 매핑)는 `head.tag` 한 곳에만 있다 — **공유 자원(22.5)**
- 사이드바 내비 항목은 feature 확정 시 `layout.tag`에 추가하고, 페이지에서 `active="{key}"`로 활성 표시

## 4. 타이포그래피

폰트: **Noto Sans KR** (Google Fonts, 400/500/700/800/900 — head.tag에서 로드). 크기·굵기는 프리셋만:

| 용도 | 유틸리티 |
|---|---|
| 페이지 제목 | `text-2xl font-black tracking-tight` |
| 섹션 제목 | `text-lg font-extrabold` |
| 카드·모달 제목 | `text-sm font-extrabold` |
| 본문·테이블 셀 | `text-sm` |
| 보조 텍스트 | `text-xs text-muted-foreground` |
| 폼 라벨·테이블 헤더 | `text-xs font-extrabold text-muted-foreground` |
| 통계 값 | `text-2xl font-black tracking-tight` |

## 5. 간격 · 라운드 · 그림자

- 카드 내부 패딩 `p-5`, 페이지 본문 `p-7`, 카드 사이 `gap-4`, 통계 그리드 `gap-4`
- 라운드: 카드·모달 `rounded-lg`(=`--radius` 12px)/`rounded-xl`, 버튼·입력 `rounded-md`, 배지 `rounded-full`
- 그림자: 카드 **없음**(보더만), 모달·토스트·데스크톱 인증 셸만 `shadow-xl`/`shadow-lg`
- 반응형: 모바일 퍼스트, 브레이크포인트는 `md`/`lg`만 (컨벤션 13.2). 사이드바는 `lg` 미만에서 메뉴 버튼으로 여는 서랍이 되며 가로 내비로 축약하지 않는다
- 모든 주요 조작은 최소 `44px` 터치 영역을 확보한다. 작은 아이콘은 시각 크기와 별개로 버튼 영역을 `size-11`로 확장한다

## 6. 컴포넌트 태그 (attribute 명세)

| 태그 | attributes | 비고 |
|---|---|---|
| `<t:pageHead>` | `title`*, `description`, body=우측 액션 버튼 | 페이지 최상단 1회 |
| `<t:card>` | `title`, `moreUrl`, `moreLabel`, `flush`(Boolean) | `flush=true`면 본문 패딩 제거 — 테이블/리스트 카드용 |
| `<t:statCard>` | `label`*, `value`*, `unit`, `delta`, `tone`(default·success·danger), `featured`(Boolean), `valueHook`, `deltaHook` | 대시보드 통계 타일. 동적 통계는 hook으로 값·증감 영역만 갱신. 한 그룹에서 핵심 지표 하나만 `featured=true` |
| `<t:badge>` | `tone`*(accent·success·warning·danger·info·neutral), `dot`(Boolean), body=텍스트 | 상태 표시 |
| `<t:emptyState>` | `title`*, `message`, body=행동 유도 버튼 | 빈 목록/검색 결과 |
| `<t:modal>` | `id`*, `title`*, `description`, `footer`(fragment), body | 기본 hidden. `openModal(id)`로 열기 |
| `<t:formField>` | `label`*, `path`*, `type`(text·email·password·number·textarea), `required`(Boolean), `help` | `<form:form>` 내부 전용. errors 자동 출력 |
| `<t:button>` | `type`, `href`, `variant`(primary·outline·dark·danger), `size`(default·compact), `action`, `pageAction`, `openModal`, `confirm`, `confirmAction`, `cssClass` | 기본 높이 44px. `href` 지정 시 같은 외형의 링크로 렌더링. 공통 이벤트 규약과 버튼 위계를 한 곳에서 관리 |
| `<t:dataTable>` | `caption`*, `cssClass`, body=`thead`·`tbody` | 가로 스크롤과 접근성 caption, `th`·`td`·`tr` 기본 스타일을 관리. 셀별 정렬·강조·상태색만 페이지에 작성 |
| `<t:filterChip>` | `group`*, `value`*, `label`*, `active`(Boolean), `count`, `dot`(Boolean) | 필터 그룹·값, `aria-pressed`, 활성·비활성 스타일을 관리. JS에서는 `activateFilterChip(button)` 사용 |
| `<t:markdown>` | `html`* (`SafeMarkdownHtml`) | 서버 sanitizer를 통과한 내부 공지 Markdown HTML만 출력. 일반 문자열·사용자 입력은 전달 금지 |

(*=필수) select·라디오 등은 첫 실사용 때 formField에 확장하거나 레시피로 추가한다.

### 6.1 상호작용 규약 (공통 JS — layout 셸이 자동 로드)

| 규약 | 사용법 |
|---|---|
| **토스트** | `js/common/toast.js`의 `showToast(message)` |
| **flash 메시지** (SSR 표준 흐름) | 컨트롤러에서 `redirectAttributes.addFlashAttribute("toast", "저장되었습니다.")` → 리다이렉트 후 layout이 자동으로 토스트 표시. POST 성공 응답은 이 패턴을 기본으로 한다 |
| **모달** | `<t:modal>` + `openModal(id)` 또는 버튼에 `data-open-modal="모달id"`(페이지 JS 불필요). ESC·배경 클릭·`data-action="close-modal"` 닫기, 포커스 트랩, 배경 스크롤 잠금 내장 |
| **confirm** (파괴적 동작 — 컨벤션 13.3 이행 수단) | 폼 submit 버튼에 `data-confirm="정말 삭제할까요?"` 부착 → 공용 확인 모달(관리자 셸 내장)이 가로채고, 확인 시 폼 제출 |
| **중복 제출 방지** | 상태 변경 폼에 `data-guard="true"` 기본 부착 → 제출 시 재제출 차단 + submit 버튼 비활성 표시. 제출 버튼의 `name`/`value`에 의존하지 않는다 |

- confirm 모달은 관리자 셸(layout.tag)에만 내장 — 공개 셸에서 파괴적 동작이 필요해지면 그때 layoutPublic에 추가
- 버튼 문구는 실행 동작 그대로("삭제", "저장") — "확인/제출" 같은 중립 문구 금지, flash 문구는 완료형("저장되었습니다.")

## 7. 레시피 (태그화하지 않은 조합)

각 레시피는 실제 화면에 적용하며, 반복이 확인되면 태그 파일로 승격한다. 폐기된 화면 데모의 정적 백업은 `docs/archive/style-guide.html`에만 보관한다.

### 7.1 기본

**버튼** — base: `inline-flex min-h-11 items-center justify-center rounded-md px-4 text-sm font-bold transition-colors disabled:pointer-events-none disabled:opacity-50`

| variant | 추가 클래스 |
|---|---|
| primary | `bg-primary text-primary-foreground hover:bg-primary-strong hover:text-white focus-visible:ring-2 focus-visible:ring-ring` |
| outline | `border bg-card hover:bg-secondary` |
| dark(네이비) | `bg-sidebar text-white hover:bg-sidebar-accent` |
| danger | `bg-destructive text-destructive-foreground hover:bg-destructive/90` |
| compact | 데스크톱 전용 툴바에서만 `min-h-9 px-3 text-xs`. 모바일·터치 화면에서는 기본 높이를 유지한다 |

**테이블** — `<t:card flush="true">` 안에서 `<t:dataTable caption="목록 설명">`을 사용한다. 가로 스크롤과 `th`·`td`·`tr` 기본 스타일은 태그가 소유하며, 페이지에는 셀별 정렬·강조·상태색만 남긴다.

**칩(필터)** — `<t:filterChip group="자료유형" value="all" label="전체" active="true" count="24"/>`처럼 사용한다. 같은 `group`의 칩은 공통 JS `activateFilterChip(button)`으로 단일 활성 상태를 유지한다.

**공지 배너** — `flex items-start gap-3 rounded-lg border bg-accent/50 px-4 py-3.5` + 의미 있는 리드 아이콘과 제목. 굵은 한쪽 색상선은 사용하지 않는다

**내부 공지 게시판** — `/notices`는 중요 공지 우선 목록, 읽음·대상 범위 필터, 검색과
페이지 단위 더 보기를 사용한다. 상세는 제목·대상·게시 및 마지막 수정 시각·작성자·본문·
첨부만 보여 주며 조회수 지표를 넣지 않는다. 작성과 수정은 모달이 아니라 전용 페이지로
연다. 제목 바로 아래에는 게시 대상을 한 줄로 두며, 팀 대상은 `내 팀` 같은 내부 용어가
아니라 로그인 멤버의 실제 팀명으로 표시한다. 본문은 전체 폭으로 우선 작성하고, 작성과
미리보기는 탭으로 전환하며 동시에 좁게 나누지 않는다. 첨부는 본문 바로 아래에 둔다.
중요 여부·예약 게시 같은 보조 동작도 게시 대상과 같은 제목 아래 행에서 바로 선택하게
하며 별도의 게시 설정 섹션이나 드롭다운을 만들지 않는다. 중복된 상단 게시 버튼은 두지
않고 하단의 하나의 액션 바에서 초안 저장·게시를 처리한다.

**Markdown** — 미리보기와 상세 모두 서버가 정화한 결과를 안전 DOM 마운터 또는
`<t:markdown>`으로만 렌더링한다. 원시 HTML·외부 이미지는 지원하지 않는다. 본문 이미지는
`attachment://{storedFileId}` 내부 참조만 사용하며, 이미지 도구 모음으로 올린 JPEG·PNG·WebP
10MiB 이하 파일을 같은 첨부 목록에 표시한다. 새 글의 미리보기는 업로더 전용 URL을,
기존 글은 공지 첨부 권한을 통과한 inline URL을 사용한다. 링크는 새 창에서 열 때
`noopener noreferrer`를 함께 적용한다.
렌더링된 본문은 공지 전용 `markdown-content` 스타일을 사용해 제목 1~6, 문단, 목록,
인용, 코드, 표와 이미지의 위계를 미리보기와 상세에서 동일하게 유지한다. Tailwind
Typography 플러그인의 `prose` 클래스가 존재한다고 가정하지 않는다.

**입력(formField 밖에서 쓸 때)** — `h-10 w-full rounded-md border border-input bg-card px-3 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20`

### 7.2 리스트 · 미디어

**아바타(내부 사진·이니셜)** — 사진은 로그인 멤버만 볼 수 있는 내부 식별 정보다.
`img`는 `rounded-full object-cover`로 표시하고, 로드 실패·사진 없음에는
`flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white`
이니셜을 즉시 대체로 보인다. 외부 공개 화면에는 사용하지 않는다.

**프로필·팀 변경** — 사진 교체, 소속 팀 변경처럼 결과가 이력에 남는 작업은 데스크톱과
모바일 모두 페이지 내부 섹션에서 처리한다. PC에서 짧은 우측 패널이나 입력 모달로
밀어 넣지 않으며, 선택값·필수 사유·즉시 반영 결과를 같은 화면에서 확인한다.

**리스트 행** (일정·활동 내역 — 목업 `.row`) — 컨테이너는 `<t:card flush="true">`, 행: `flex items-center gap-3 border-b px-5 py-3 last:border-0`
- 좌측 시간: `min-w-11 text-sm font-extrabold text-accent-foreground`
- 리드 아이콘 박스: `flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-accent-foreground` (tone에 따라 `*-soft` 배경 교체)
- 본문: 제목 `text-sm font-bold`, 부가 `mt-0.5 text-xs text-muted-foreground`, 래퍼 `min-w-0 flex-1`

**진행 바** — 트랙 `h-2 overflow-hidden rounded-full bg-secondary`, 채움 `h-full rounded-full bg-primary transition-all` (완료 계열은 `bg-success`). 진행률은 서버 데이터 기반 연속값이므로 `style="width:63%"` 인라인 지정을 예외 허용한다 (컨벤션 13.2 예외 조항)

**스크롤 목록** — `max-h-96 overflow-y-auto` (카드 안에서 긴 목록을 자를 때)

### 7.3 컨트롤

**세그먼트 컨트롤** (역할 전환·기간 필터·인증 탭 — 목업 `.seg`/`.role-switch`/`.auth-tabs`) — 래퍼 `inline-flex rounded-lg border bg-secondary p-0.5`, 항목 `rounded-md px-3 py-1.5 text-xs font-bold text-muted-foreground transition-colors`, 활성 `border bg-card text-foreground`

**체크박스(커스텀)** — `<button type="button">` 기반: `flex size-5 items-center justify-center rounded-md border bg-card text-white transition-colors`, 체크 시 `border-success bg-success` + 내부 체크 svg. 토글은 `data-action` 위임(13.3)

**스테퍼(수량)** — 래퍼 `flex items-center justify-between rounded-lg border p-1.5`, 버튼 `flex size-8 items-center justify-center rounded-md border bg-card text-lg font-extrabold`, 값 `text-sm font-black`

### 7.4 캘린더 · 날짜/시간 입력

**통합 캘린더** — FullCalendar 7.0.1을 사용한다. 상단 도구는 오늘, 이전·다음,
현재 기간, 월·주·목록 보기, 실제 팀 이름 필터, 일정 등록 순으로 배치한다.
- 데스크톱·가로 태블릿은 월간, 모바일·세로 태블릿은 주간 목록을 기본으로 한다.
- 월간은 하루 최대 3건만 먼저 보이고 나머지는 `N개 더 보기`로 연다.
- 전체 일정은 네이비, 팀 일정은 `primary-strong`을 사용하며 색과 함께 실제 팀명을
  항상 표시한다.
- 일정 클릭은 읽기 상세를 먼저 연다. 수정·삭제는 권한이 있을 때만 상세에서 시작한다.
- 드래그·리사이즈로 서버 상태를 즉시 바꾸지 않는다.

**날짜·시간 입력** — 공통 `<t:dateTimeField>`를 사용한다. 모바일과 JS 실패 시
`datetime-local` 네이티브 입력을 유지하고, 데스크톱 초기화에 성공했을 때만 Vanilla
Calendar Pro 3.1.0 날짜 입력과 24시간제 시간 입력을 분리한다.
- 일정은 30분, 공지 예약과 활동 기록은 5분 단위다.
- 직접 입력·붙여넣기·키보드 이동을 허용하며 달력 선택만 강제하지 않는다.
- 종일 일정은 화면에서 포함형 종료일을 받고 서버 전송 직전에 다음 날 00:00의
  배타적 종료값으로 변환한다.
- 대화상자는 데스크톱에서 넓은 중앙형, 모바일에서 전체 화면형을 사용한다.

### 7.5 인증 화면 (로그인 기능 구현 시 — layoutAuth 내부)

**인증 셸** — 데스크톱에서 좌측 네이비 맥락 패널과 우측 폼을 분리한다. 좌측은 동아리 소개를 낮은 불투명도로 사용하고 제품 설명만 담으며, 우측의 폼 폭은 `max-w-md`를 넘지 않는다. 모바일에서는 맥락 패널을 숨기고 로고·페이지 제목·폼을 단일 열로 제공한다. 영문 대문자 장식 레이블, 의미 없는 원호, 테스트 계정 노출, 장식용 그라디언트 배경이나 카드 안의 카드 중첩은 사용하지 않는다.

**역할 선택 카드** — `flex w-full items-center gap-3 rounded-lg border-2 p-3.5 text-left transition-colors hover:border-primary hover:bg-accent` + 역할 아이콘 박스 `flex size-9 items-center justify-center rounded-md bg-sidebar text-sm font-black text-white`

**구분선(또는)** — `flex items-center gap-2.5 text-xs font-bold text-muted-foreground/70 before:h-px before:flex-1 before:bg-border after:h-px after:flex-1 after:bg-border`

**초대 코드** — `font-mono text-sm font-black tracking-widest text-accent-foreground`

## 8. 금지/주의 (컨벤션 13.2 재확인 + 이 시스템 고유)

- 민트 배경에 `text-white` 금지 (hover의 `primary-strong` 위에서만 허용)
- 팔레트 유틸리티(`bg-red-500`)·임의 색값 금지 — 새 색은 tokens.css + head.tag 매핑에 함께 추가
- 상태색 본색을 넓은 면적 배경으로 쓰지 않는다 (soft 배경 + 본색 글자)
- 카드에 그림자 금지, `rounded-2xl` 이상 금지 (프리셋 sm~xl만)
- 카드·배너·인용문에 `border-l-4`/`border-r-4` 굵은 색상선을 장식으로 사용하지 않는다. 배경 tint, 아이콘, 제목 위계로 의미를 전달한다
- 가로 스크롤은 표처럼 구조상 필요한 경우에만 허용하며, 모바일 주 내비에는 사용하지 않는다

## 9. 아이콘

- **Lucide**(lucide.dev)에서 SVG를 복사해 **인라인**으로 쓴다 — 의존성·아이콘 폰트 없음
- 복사 시 속성 고정: `viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"` (+ `stroke-linecap="round" stroke-linejoin="round"`)
- 크기는 `size-4`(16px)/`size-5` 유틸리티, 색은 지정하지 않고 부모의 `text-*`에서 `currentColor`로 상속받는다
- 아이콘마다 태그 파일을 만들지 않는다 — 같은 아이콘이 여러 화면에서 반복되면 그 아이콘을 포함한 **UI 블록** 단위로 태그화한다
- 장식 아이콘에는 `aria-hidden="true"`, 의미 전달 아이콘에는 텍스트 라벨 병기

## 10. 포맷 · 반응형 · 빌드 결정 (확정 사항)

- **날짜/시간/금액 표기** — JSTL `fmt` 태그로 통일 (컨벤션 12.1 taglib 목록의 `fmt`):
  - 날짜 `yyyy.MM.dd(E)` → `2026.07.12(일)`, 시간 `HH:mm` → `19:00`
  - 금액 `<fmt:formatNumber value="${...}" pattern="#,##0"/>원` → `15,000원`
- **테이블 모바일 전략** — 카드 전환 없이 `overflow-x-auto` 가로 스크롤로 통일 (7.1 테이블 레시피의 래퍼 필수)
- **Tailwind Play CDN은 prod에서도 유지** — 버전·SRI 고정 전제. 런타임 컴파일 특성상 첫 페인트 깜빡임(FOUC)이 있음을 인지하고 수용(서비스 규모 판단). 트래픽·성능 문제가 실측되면 빌드 통합(Node)으로 재검토
