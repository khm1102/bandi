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

저장소를 서버에 가져온 뒤 환경 파일을 만든다. 실제 환경 파일은 GitHub Actions의
checkout 정리 대상이 되지 않도록 저장소 작업 폴더 밖에 둔다.

```bash
sudo install -d -m 700 /etc/bandi
sudo cp .env.test.example /etc/bandi/.env.test
sudo chown <runner-user>:<runner-user> /etc/bandi/.env.test
sudo chmod 600 /etc/bandi/.env.test
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
docker compose --env-file /etc/bandi/.env.test -f docker-compose.test.yaml up -d --build
docker compose --env-file /etc/bandi/.env.test -f docker-compose.test.yaml ps
docker compose --env-file /etc/bandi/.env.test -f docker-compose.test.yaml logs -f app
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
docker compose --env-file /etc/bandi/.env.test -f docker-compose.test.yaml exec -T mysql \
  mysqldump -ubandi -pbandi1234 --single-transaction bandi > backups/bandi.sql
```

파일은 `/data/bandi` 전체를 별도 보관소에 복사해 함께 백업한다. 자동 백업은 이 단계에
포함하지 않는다.

## 5. 업데이트와 중지

```bash
docker compose --env-file /etc/bandi/.env.test -f docker-compose.test.yaml down
```

자동 배포가 설정된 뒤에는 `dev` 병합이 테스트 서버를 갱신한다. 긴급한 수동 재배포는
GitHub Actions의 `Verify and deploy test` 워크플로에서 `deploy`를 선택해 실행한다. 이미지
변경 후에도 MySQL volume과 `/data/bandi` bind mount는 유지된다. Flyway는 새 마이그레이션만
적용하며, 적용된 마이그레이션 파일을 수정하지 않는다.

자동 배포는 `app`, `cloudflared` 서비스만 `--no-deps`로 다시 만든다. 이미 운영 중인 `mysql`
컨테이너와 `bandi-test-mysql-data` named volume에는 `down -v`, `rm`, `volume prune`, 초기화 명령을
실행하지 않는다. 데이터 삭제가 필요한 경우에는 별도 백업·승인 절차로만 수행한다.

## 6. GitHub Actions 셀프 호스티드 러너

PR 검증은 GitHub 호스티드 러너에서 실행한다. 테스트 서버의 셀프 호스티드 러너는 `dev` 푸시와
명시적 수동 재배포에서만 실행하므로, PR 코드가 서버의 Docker 권한·Cloudflare 토큰·파일 볼륨에
접근하지 못한다.

### 6.1 서버 준비

러너는 전용 Linux 사용자로 설치하고 Docker 명령을 실행할 수 있어야 한다. `<runner-user>`에는
실제 러너 계정명을 넣는다.

```bash
sudo useradd --create-home --shell /bin/bash <runner-user>
sudo usermod -aG docker <runner-user>
sudo install -d -o <runner-user> -g <runner-user> -m 755 /opt/actions-runner
sudo install -d -o 10001 -g 10001 -m 750 /data/bandi
```

`docker` 그룹 권한을 적용한 뒤에는 해당 계정으로 새 로그인 세션을 연다. 러너는 root로 실행하지
않으며, `sudo` 권한을 부여하지 않는다.

### 6.2 러너 등록

GitHub 저장소의 **Settings → Actions → Runners → New self-hosted runner**에서 Linux x64 등록
명령과 일회성 토큰을 새로 받는다. 서버에서 `<runner-user>`로 다음을 실행한다.

```bash
cd /opt/actions-runner
# GitHub 화면이 안내한 압축 파일을 내려받아 푼 뒤 실행한다.
./config.sh --url https://github.com/khm1102/bandi --token <one-time-token> \
  --labels bandi-test --unattended
sudo ./svc.sh install <runner-user>
sudo ./svc.sh start
```

등록 후 GitHub 화면에서 `self-hosted`, `linux`, `x64`, `bandi-test` 레이블과 `Idle` 상태를 확인한다.
등록 토큰은 짧은 시간만 유효하므로 문서·환경 파일·로그에 보관하지 않는다.

### 6.3 워크플로 동작과 운영 규칙

- `pull_request → dev`: GitHub 호스티드 러너에서 MySQL 8.4와 `./gradlew build`만 실행한다.
- `push → dev`: 검증 성공 뒤 `bandi-test` 셀프 호스티드 러너가 Docker Compose를 재배포한다.
- 수동 실행: Actions 화면에서 `deploy`를 체크한 경우에만 재배포한다.
- 배포는 `bandi-test-deploy` 동시성 그룹으로 직렬화한다. 이전 배포를 취소하지 않는다.
- 배포는 `app`, `cloudflared`만 다시 만들며, 기존 MySQL 컨테이너·named volume을 재생성하거나
  삭제하지 않는다.
- 배포 시 `/etc/bandi/.env.test`의 존재·권한, Compose 설정, app·mysql·cloudflared 기동과 파일
  저장 경로 쓰기 가능 여부를 확인한다. 실패하면 서비스 상태와 최근 로그가 작업 로그에 남는다.

`dev` 보호 규칙에는 `Verify build and test` 상태 검사를 병합 필수로 추가한다. 직접 `dev` 푸시는
허용하지 않는 것이 배포 전 검증을 보장하는 방법이다.
