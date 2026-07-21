# 서비스 폐기 운영 절차

## 1. 목적

공연·관람 기능을 DB와 애플리케이션에서 제거하기 전에 MinIO 객체를 먼저 안전하게 정리한다.
이 절차는 `performance_file_retirement_manifest`에 근거를 남기므로, 객체 삭제 실패나 유지 서비스의
공유 참조를 확인하지 못한 상태에서 테이블을 먼저 제거할 수 없다.

## 2. 대상과 제외

대상은 공연 공개 페이지 이미지, 공개 프로필 사진, 공연 미디어가 참조하는 파일과
`performance/` 접두사의 객체다.

다음 서비스가 참조하는 파일은 대상에서 제외하고 `SKIPPED`로 기록한다.

- 소품·장비 사진
- 자료실 파일
- 활동 기록 파일
- 공지·공시 첨부 파일

`testPW.txt`와 `.bandi-review-stage.tar.gz`는 이 절차와 무관하며 읽거나 변경하지 않는다.

## 3. 1단계 실행

기본 애플리케이션 기동에서는 retirement 실행기가 동작하지 않는다.

먼저 후보만 기록한다.

```bash
./gradlew bootRun --args='--bandi.retirement.performance-files.enabled=true'
```

로그의 후보·보류 건수를 확인한 뒤, 실제 객체 삭제를 승인할 때만 아래처럼 실행한다.

```bash
./gradlew bootRun --args='--bandi.retirement.performance-files.enabled=true --bandi.retirement.performance-files.mode=APPLY'
```

`APPLY`는 삭제 직전에 유지 서비스의 참조를 다시 검사한다. 공유 참조는 `SKIPPED`로 전환하고,
MinIO 삭제가 실패하면 해당 파일을 `FAILED`로 기록한 뒤 즉시 중단한다. 앱이 기동 상태로 남으므로
완료 로그를 확인한 뒤 운영자가 종료한다.

## 4. 2단계 시작 조건

다음 조건을 모두 만족해야 서비스·API·JSP·테이블을 제거하는 2단계 마이그레이션을 적용한다.

```sql
SELECT status_code, COUNT(*)
FROM performance_file_retirement_manifest
GROUP BY status_code;
```

- `PENDING`, `FAILED`가 0건이다.
- `DELETED`는 MinIO 객체 삭제가 완료된 행이다.
- `SKIPPED`는 유지 서비스의 참조가 실제로 남아 있는 행이다.

2단계 마이그레이션은 위 조건을 다시 검사하고, `DELETED`인 `stored_file` 행만 하드 삭제한다.
그 뒤 임시 manifest를 제거한다.
