# 활동 내역서 HWPX 운영 안내

## 1. 기능과 정본

- 화면: `/activity-documents`
- 빈 양식: `GET /api/activity-report-documents/blank`
- 완성본: `POST /api/activity-report-documents`
- 런타임 정본: `src/main/resources/templates/hwpx/bandi-activity-report-template.hwpx`
- 정본 생성 스크립트: `scripts/sanitize_activity_report_template.py`

HWPX는 OWPML 기반 ZIP/XML 문서 형식이다. 구현과 정본 검증은
[한컴 공식 HWPX 포맷 안내](https://tech.hancom.com/hwpxformat/)를 기준으로 한다.
원본 예시 파일은 실제 개인정보와 사진을 포함하므로 Git 정본으로 사용하지 않는다.
원본에서 개인정보, 예시 사진, 미리보기와 작성자 메타데이터를 제거한 런타임 정본만
애플리케이션 자원에 둔다.

## 2. 개인정보와 저장 정책

대표자, 장소, 일시, 활동 내용, 참여자와 사진은 HWPX 생성 요청 중에만 처리한다.
DB, `stored_file`, `FILE_STORAGE_ROOT`, 세션, 로그, 브라우저 저장소에 남기지 않는다.
응답에는 `Cache-Control: no-store`를 사용한다.

사진은 JPEG·PNG, 최대 10MiB만 허용한다. 디코딩 전 최대 40MP와 한 변 12,000px를
검사하고, EXIF 방향을 적용한 뒤 메타데이터를 제거한다. 결과 이미지는 1600×1200 흰
캔버스에 비율 유지 `contain` 방식으로 넣으며 크롭하지 않는다.

## 3. 현재 회장 지정

회장은 애플리케이션 권한과 별개의 `club_officer` 직책이다. 실제 회장 멤버가 먼저
등록되어 있고 활성 상태여야 한다. 아래 확인·배정 SQL은 운영자가 실제 학번으로 실행한다.

```sql
SELECT member_id, student_no, name, member_status_code
FROM member
WHERE student_no = :actual_student_no;

INSERT INTO club_officer (position_code, member_id, appointed_dttm)
SELECT 'PRESIDENT', member_id, CURRENT_TIMESTAMP
FROM member
WHERE student_no = :actual_student_no
  AND member_status_code = 'ACTIVE'
ON DUPLICATE KEY UPDATE
    member_id = VALUES(member_id),
    appointed_dttm = VALUES(appointed_dttm),
    updated_dttm = CURRENT_TIMESTAMP;
```

실제 학번을 알 수 없는 상태에서 원동연 멤버나 직책을 운영 Flyway에 가짜로 넣지 않는다.
회장이 없거나 비활성 상태면 생성과 빈 양식 다운로드는 409로 차단된다.

## 4. 정본 재생성

기준 양식이 승인된 새 파일로 바뀔 때만 정본을 다시 만든다. 스크립트는 예시 개인정보가
제거됐는지, 표 구조가 2페이지·14명 기준인지, 동적 표식과 이미지 참조가 유효한지 확인한다.
정본 변경 후에는 전체 자동 테스트와 한글 프로그램 수동 검증을 모두 다시 수행한다.

## 5. 한글 수동 검증

다음 네 파일을 한글에서 열고 편집 후 HWPX로 다시 저장한다.

1. 빈 양식
2. 일반 완성본
3. 세로 사진 완성본
4. 참여자 14명·활동 내용 300자 경계 완성본

각 파일에서 2페이지, 셀·폰트·여백, 고정 안내 문구, 사진 위치와 비율, 참여자 표와
하단 회장 이름을 확인한다. 자동 ZIP/XML 검증 통과만으로 한글 레이아웃 검증을 대체하지 않는다.
