# 외부 공시 폐기 운영 절차

외부 공시를 제거하기 전에 첨부 파일을 로컬 영구 볼륨에서 안전하게 파기하는 1단계
절차다. 이 단계에서는 공시 화면·API·테이블을 제거하지 않는다.

## 전제

- 파일 저장 루트(`FILE_STORAGE_ROOT`)가 실제 운영 볼륨을 가리켜야 한다.
- 작업 전 DB와 파일 볼륨의 백업 또는 파기 승인 기록을 남긴다.
- 실행 중인 애플리케이션과 같은 배포본·환경 변수·DB를 사용한다.

## 1. 후보 확인

다음 명령은 manifest를 최신 공시 첨부 기준으로 동기화하고 현황만 기록한다. 디스크
파일과 `stored_file` 메타데이터는 삭제하지 않는다.

```bash
./gradlew bootRun --args='--spring.main.web-application-type=none --bandi.retirement.public-notice.mode=REPORT'
```

로그의 `total`, `pending`, `deleted`, `retainedShared`, `failed` 수를 기록한다.
`RETAINED_SHARED`는 내부 공지·자료·활동 기록·소품 사진이 같은 `stored_file`을 참조해
보존되는 파일이다.

## 2. 파일 파기

후보와 보존 수를 확인한 뒤에만 다음 명령을 실행한다.

```bash
./gradlew bootRun --args='--spring.main.web-application-type=none --bandi.retirement.public-notice.mode=APPLY'
```

로컬 파일이 이미 없으면 성공(`DELETED`)으로 처리한다. 파일 시스템 오류는 `FAILED`로
기록하며, 다른 후보는 계속 처리한다. 실패가 하나라도 있으면 2단계 제거를 진행하지
않고 원인을 해결한 뒤 같은 `APPLY` 명령을 다시 실행한다.

## 3. 2단계 진행 조건

아래 SQL의 결과가 없고, 새 첨부가 생성되지 않았음을 확인한 뒤에만 별도 PR의 2단계
마이그레이션을 적용한다.

```sql
SELECT public_notice_retirement_manifest_id, stored_file_id, retirement_status_code
FROM public_notice_retirement_manifest
WHERE retirement_status_code NOT IN ('DELETED', 'RETAINED_SHARED');
```

2단계는 외부 공시 코드·URL·API·테이블을 제거하고, `DELETED` 상태이며 다른 서비스가
참조하지 않는 `stored_file` 메타데이터만 하드 삭제한다. 내부 공지·자료·활동 기록·소품
파일은 이 절차에서 삭제하지 않는다.
