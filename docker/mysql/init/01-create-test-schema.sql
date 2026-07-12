-- 테스트 전용 스키마 (컨벤션 17.3 — 동일 컨테이너의 별도 스키마)
CREATE DATABASE IF NOT EXISTS bandi_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON bandi_test.* TO 'bandi'@'%';
FLUSH PRIVILEGES;
