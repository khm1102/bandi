# 테스트 서버 배포

이 문서는 팀 공유용 테스트 서버에 Bandi를 배포하는 절차다. 구성은 Docker Compose의
`app`, `mysql`, `cloudflared` 세 서비스다. 외부 공개는 Cloudflare Tunnel만 사용하며,
애플리케이션과 MySQL 포트는 호스트에 노출하지 않는다.

## 1. 서버 준비

서버에 Docker Engine과 Docker Compose plugin을 설치한다. 파일 저장 경로는 컨테이너의
실행 사용자 UID/GID `10001`이 읽고 쓸 수 있어야 한다.

```bash
sudo install -d -o 10001 -g 10001 -m 750 /data/bandi
```

저장소를 서버에 가져온 뒤 환경 파일을 만든다.

```bash
cp .env.test.example .env.test
```

테스트 서버에서는 DB 비밀번호를 팀 공통 테스트 값으로 사용할 수 있다. 다만
`CLOUDFLARE_TUNNEL_TOKEN`은 Cloudflare Dashboard에서 생성한 실제 토큰을 `.env.test`에만
입력하고 Git에 커밋하지 않는다.

## 2. Cloudflare Tunnel 설정

Cloudflare Dashboard에서 Tunnel을 만들고 Public Hostname을 다음처럼 연결한다.

```text
bandi.khm1102.com -> http://app:8080
```

Compose의 `cloudflared`는 토큰으로 Cloudflare에 outbound 연결한다. 서버 방화벽에서 앱
`8080`과 MySQL `3306`을 열 필요가 없다. 팀원만 사용하는 서버라면 Cloudflare Access에서
팀원 계정만 허용한다.

## 3. 기동과 확인

```bash
docker compose --env-file .env.test -f docker-compose.test.yaml up -d --build
docker compose --env-file .env.test -f docker-compose.test.yaml ps
docker compose --env-file .env.test -f docker-compose.test.yaml logs -f app
```

앱은 `prod` 프로필로 실행되고 Flyway가 `bandi` 스키마를 자동 적용한다. Cloudflare에서
HTTPS를 종료하므로 외부 사용자는 `https://bandi.khm1102.com`으로만 접속한다. Compose는
`SERVER_FORWARD_HEADERS_STRATEGY=framework`를 주입해 프록시의 HTTPS 헤더를 처리한다.

## 4. 데이터 보존과 백업

- MySQL 데이터는 Docker named volume `bandi-test-mysql-data`에 보관된다.
- 업로드 파일은 호스트 `/data/bandi`에 보관된다.
- `docker compose down`은 위 데이터를 지우지 않는다.
- `docker compose down -v`는 MySQL volume을 지우므로 사용하지 않는다.

수동 DB 백업 예시는 다음과 같다. 백업 디렉터리는 서버에서 접근을 제한한다.

```bash
mkdir -p backups
docker compose --env-file .env.test -f docker-compose.test.yaml exec -T mysql \
  mysqldump -ubandi -pbandi1234 --single-transaction bandi > backups/bandi.sql
```

파일은 `/data/bandi` 전체를 별도 보관소에 복사해 함께 백업한다. 자동 백업은 이 단계에
포함하지 않는다.

## 5. 업데이트와 중지

```bash
git pull --ff-only origin dev
docker compose --env-file .env.test -f docker-compose.test.yaml up -d --build
docker compose --env-file .env.test -f docker-compose.test.yaml down
```

이미지 변경 후에도 MySQL volume과 `/data/bandi` bind mount는 유지된다. Flyway는 새
마이그레이션만 적용하며, 적용된 마이그레이션 파일을 수정하지 않는다.
