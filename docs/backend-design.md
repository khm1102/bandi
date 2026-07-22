# bandi 백엔드 설계

> 문서 상태: 현행 구현 기준
> 기준 문서: `feature-spec.md`, `database-schema.md`, `coding-convention.md`
> 작성 기준일: 2026-07-22

## 1. 목적

bandi는 학교 SSO로 확인된 동아리 멤버가 일정, 공시·공지·자료, 활동 기록,
소품·장비와 멤버 권한을 운영하는 SSR 웹 서비스다.

학교 비밀번호와 학교 세션은 인증 요청 중에만 사용하며, DB·세션·로그에 저장하지
않는다. 파일 바이너리는 `FILE_STORAGE_ROOT` 아래 로컬 영구 볼륨에 저장하고,
DB에는 메타데이터만 보관한다.

## 2. 구조

```text
Controller → Service → Mapper → Model
                     ↓
              다른 feature의 Service
```

- Controller는 HTTP 요청 검증, 로그인 멤버 식별, SSR 뷰 또는 API 응답을 담당한다.
- Service는 트랜잭션, 권한 범위와 유스케이스 흐름을 담당한다.
- Mapper는 자기 feature의 영속성만 담당한다.
- Model은 상태와 값 검증을 담당한다.
- 인증의 URL 경계는 Spring Security가, 팀·대상·상태 기반 권한은 Service가 최종
  검증한다.

## 3. 현행 feature

| feature | 책임 |
| --- | --- |
| `member` | 멤버 사전 등록, 학교 SSO 연결, 팀·기수·역할·상태와 이력 |
| `calendar` | 전체·팀 일정 관리 |
| `notice` | 외부 공시와 내부 공지, 읽음 상태 및 첨부 연결 |
| `resource` | 전체·팀 자료와 파일 리비전 |
| `activity` | 팀 활동 기록, 증빙 사진, 검토와 수정 이력 |
| `asset` | 소품·장비 품목, 개별 장비, 상태 이력과 사진 연결 |
| `file` | 파일 메타데이터와 로컬 파일 저장·전송 |
| `dashboard` | 각 feature의 읽기 전용 요약 |
| `audit` | 주요 운영 변경 감사 기록 |

## 4. HTTP 경계

- 페이지 조회는 JSP SSR Controller를 사용한다.
- 부분 갱신과 상태 변경은 `/api/**`에서 세션 인증과 CSRF 보호를 적용한다.
- Swagger 계약은 `global.swagger` 인터페이스에만 둔다.
- 파일은 Spring Boot가 권한을 확인한 뒤 직접 스트리밍한다. 공개 공시 첨부만
  비로그인 열람을 허용한다.

## 5. 후속 범위

온보딩은 별도 승인 전까지 설계 문서만 유지하며, 현재 구현에는 포함하지 않는다.
