# 프론트엔드 제3자 라이브러리

## 관리 원칙

- 운영 화면은 외부 CDN에 의존하지 않고 검증한 배포 파일을 저장소에 버전 고정한다.
- 원본 라이선스 파일을 해당 JavaScript 배포 디렉터리에 함께 보관한다.
- 업데이트는 별도 이슈와 PR에서 릴리스 노트, 브라우저 지원, 접근성, 번들 파일 해시와
  회귀 테스트를 확인한 뒤 버전 디렉터리를 새로 추가한다. 기존 버전 파일을 덮어쓰지 않는다.
- `build.gradle.kts`나 npm 프로젝트는 추가하지 않는다. npm tarball은 배포 파일을
  확보하고 출처를 확인하는 용도로만 사용한다.

## FullCalendar

| 항목 | 값 |
| --- | --- |
| 버전 | 7.0.1 |
| 출처 | `https://www.npmjs.com/package/fullcalendar/v/7.0.1` |
| 라이선스 | MIT |
| JavaScript | `/js/vendor/fullcalendar/7.0.1/` |
| CSS | `/css/vendor/fullcalendar/7.0.1/` |
| 사용 범위 | 통합 캘린더 월·주·목록 보기, 기간 선택, 한국어 locale |

`all/global.js`, 한국어 locale과 classic theme 배포본을 사용한다. 원본 skeleton/theme/
palette는 수정하지 않고 `bandi-adapter.css`에서 프로젝트 토큰만 연결한다.

## Vanilla Calendar Pro

| 항목 | 값 |
| --- | --- |
| 버전 | 3.1.0 |
| 출처 | `https://www.npmjs.com/package/vanilla-calendar-pro/v/3.1.0` |
| 라이선스 | MIT |
| JavaScript | `/js/vendor/vanilla-calendar-pro/3.1.0/` |
| CSS | `/css/vendor/vanilla-calendar-pro/3.1.0/` |
| 사용 범위 | 데스크톱 날짜 선택, 공지 예약·활동 기록·일정 입력 |

원본 layout/light theme는 수정하지 않는다. `bandi-adapter.css`가 선택일, 오늘, 포커스와
배경을 프로젝트 시맨틱 토큰으로 덮는다. 모바일과 초기화 실패 환경은 브라우저 네이티브
입력을 사용하므로 라이브러리 장애가 폼 입력을 막지 않는다.
