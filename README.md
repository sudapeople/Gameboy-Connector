# GameBoy Connector Plugin

GameBoy.kr 웹서버와 통신하는 마인크래프트 플러그인입니다.

## 📋 개요

이 플러그인은 GameBoy.kr 웹사이트와 실시간으로 통신하여 다음 기능을 제공합니다:

- **플레이어 목록 제공**: 웹서버에서 접속한 플레이어 목록 조회
- **명령어 실행**: 웹서버에서 마인크래프트 명령어 실행
- **서버 정보 수집**: 서버 상태, 성능, 설정 정보 전송
- **대체문자 처리**: `<player>` 등의 대체문자를 실제 값으로 변환
- **외부 IP 자동 수집**: 서버의 외부 IP 주소 자동 감지
- **동적 프로토콜 버전**: 마인크래프트 버전에 따른 프로토콜 버전 자동 감지
- **실제 TPS 측정**: Paper 서버에서 실제 TPS 측정
- **Circuit Breaker 패턴**: 웹서버 연결 실패 시 자동 복구 및 부하 방지
- **Exponential Backoff**: 연속 실패 시 대기 시간 점진적 증가로 안정성 향상

## 🔧 기술 스택

- **Java 17**: 플러그인 개발 언어
- **Bukkit/Spigot API**: 마인크래프트 서버 API
- **Gson**: JSON 처리 라이브러리
- **SnakeYAML**: YAML 설정 파일 처리
- **Gradle**: 빌드 도구

## 🚀 설치 및 설정

### 1. 플러그인 빌드

```bash
cd gameboy-connector
./gradlew shadowJar
```

빌드된 JAR 파일은 `build/libs/GameboyConnector.jar`에 생성됩니다.

### 2. 서버 설치

1. 빌드된 JAR 파일을 서버의 `plugins` 폴더에 복사
2. 서버 재시작
3. 플러그인이 자동으로 `server-code.yml` 파일을 생성

### 3. 웹서버 등록

1. `plugins/GameboyConnector/server-code.yml` 파일에서 서버 코드 확인
2. GameBoy.kr 웹사이트에서 해당 서버 코드로 서버 등록
3. 플러그인이 자동으로 서버 정보를 웹서버에 전송

## 📁 파일 구조

```
gameboy-connector/
├── src/main/java/com/gameboy/connector/
│   ├── GameboyConnector.java          # 메인 플러그인 클래스
│   ├── config/
│   │   └── ServerCodeConfig.java      # 서버 코드 설정 클래스
│   ├── models/
│   │   ├── PlayerInfo.java            # 플레이어 정보 모델
│   │   ├── ServerInfo.java            # 서버 정보 모델
│   │   └── PluginInfo.java            # 플러그인 정보 모델
│   ├── communication/
│   │   └── WebServerCommunicator.java # 웹서버 통신 클래스
│   ├── commands/
│   │   └── GameboyCommand.java        # 명령어 처리 클래스
│   ├── listeners/
│   │   └── PlayerEventListener.java   # 플레이어 이벤트 리스너
│   └── utils/
│       └── Logger.java                # 로거 유틸리티
├── src/main/resources/
│   ├── plugin.yml                     # 플러그인 메타데이터
│   └── config.yml                     # 기본 설정 파일
├── build.gradle                       # Gradle 빌드 설정
└── README.md                          # 이 파일
```

## ⚙️ 설정

### config.yml

```yaml
# 플러그인 기본 설정
plugin:
  debug: false
  update_check: true

# 웹서버 통신 설정 (API URL은 플러그인 내부에서 관리)
web_server:
  update_interval: 60
  # 연결 타임아웃 설정 (초)
  connect_timeout: 3  # 연결 시도 타임아웃
  request_timeout: 5  # 요청 응답 타임아웃
  # Circuit Breaker 설정
  failure_threshold: 3  # 연속 실패 임계값
  circuit_reset_time: 30  # Circuit 재시도 대기 시간 (초)
  backoff_base_time: 5  # 백오프 기본 시간 (초)
  max_backoff_time: 60  # 최대 백오프 시간 (초)

# 서버 정보 수집 설정
data_collection:
  collect_server_info: true
  collect_player_list: true
  collect_performance: true
  collect_plugins: true

# 명령어 실행 설정 (모든 명령어 허용)
command_execution:
  max_command_length: 1000

# 로깅 설정
logging:
  log_commands: true
  log_errors: true
  log_performance: false
```

### server-code.yml (자동 생성)

```yaml
# GameBoy Connector 서버 코드 설정
server:
  code: "GB-A1B2-C3D4-E5F6"
  name: "My Server"
  description: "자동 생성된 서버"
  created_at: "2024-01-15T14:30:00Z"
  last_updated: "2024-01-15T14:30:00Z"

# 웹서버 연동 설정 (보안상 API URL은 플러그인 내부 코드에서만 관리)
web_server:
  update_interval: 60

# 서버 정보 수집 설정
data_collection:
  collect_server_info: true
  collect_player_list: true
  collect_performance: true
  collect_plugins: true
```

## 🎮 명령어

### `/gameboy` 명령어

- `/gameboy reload` - 설정 다시 로드
- `/gameboy info` - 서버 정보 표시
- `/gameboy test` - 웹서버 연결 테스트

**권한**: `gameboy.admin` (기본적으로 OP만 사용 가능)

## 🔌 API 통신

플러그인은 다음 엔드포인트들과 통신합니다:
- `/plugin-player-list.php` - 플레이어 목록 전송 (10초마다)
- `/plugin-server-info.php` - 서버 정보 전송 (1분마다)
- `/pending-commands.php` - 명령어 폴링 및 상태 업데이트 (5초마다)
- `/plugin-test.php` - 웹서버 연결 테스트
- `/server-shutdown.php` - 서버 종료 알림
- `/command-result.php` - 명령어 실행 결과 전송

### 플레이어 목록 전송

플러그인은 10초마다 온라인 플레이어 목록을 웹서버로 전송합니다.

```json
{
  "server_code": "GB-A1B2-C3D4-E5F6",
  "timestamp": "2024-01-15T14:30:00Z",
  "online_players": [
    {
      "player_id": "플레이어1",
      "player_uuid": "550e8400-e29b-41d4-a716-446655440000"
    }
  ],
  "total_players": 1
}
```

### 서버 정보 전송

플러그인은 1분마다 서버 정보를 웹서버로 전송합니다.

```json
{
  "server_code": "GB-A1B2-C3D4-E5F6",
  "timestamp": "2024-01-15T14:30:00Z",
  "server_info": {
    "server_name": "My Server",
    "internal_ip": "192.168.1.100",
    "external_ip": "203.0.113.1",
    "server_port": 25565,
    "motd": "Welcome to My Server!",
    "version": "1.20.4",
    "protocol_version": 765,
    "max_players": 64,
    "online_players": 15,
    "tps": 20.0,
    "server_uptime": 3600000,
    "memory_usage": {
      "used": 2684354560,
      "total": 4294967296,
      "percentage": 62.5
    },
    "java_version": "21.0.1",
    "os_info": "Linux Ubuntu 22.04 LTS",
    "server_settings": {
      "gamemode": "survival",
      "difficulty": "normal",
      "pvp_enabled": true,
      "whitelist_enabled": false
    },
    "world_info": {
      "world_name": "world",
      "world_size": 1048576000,
      "chunks_loaded": 1024
    },
    "plugins": [
      {
        "name": "GameboyConnector",
        "version": "1.1.0",
        "enabled": true
      }
    ],
    "security_info": {
      "op_count": 2
    }
  }
}
```

### 명령어 실행

웹서버의 명령어 큐를 5초마다 폴링하여 대기 중인 명령어를 가져와 실행합니다.

**명령어 폴링 요청**:
```
GET /pending-commands.php?server_code=GB-A1B2-C3D4-E5F6
```

**응답 예시**:
```json
{
  "status": "success",
  "pending_commands": [
    {
      "id": 123,
      "player_name": "플레이어1",
      "command_string": "roulette_reward <player>"
    }
  ]
}
```

**실행 과정**:
1. 명령어 상태를 `processing`으로 변경
2. `<player>` → `플레이어1`로 변환
3. `roulette_reward 플레이어1` 명령어 실행
4. 실행 결과에 따라 상태를 `completed` 또는 `failed`로 업데이트

## 🔐 보안

- **API URL 보안**: API URL은 플러그인 내부 코드에서만 관리
- **서버 코드 시스템**: 고유한 서버 식별 코드로 서버 구분
- **인증**: 서버 코드 기반 인증 (API 키 불필요)
- **명령어 실행**: 모든 명령어 실행 가능 (보안상 로그 제거)
- **외부 IP 검증**: 사설 IP 필터링 및 유효성 검사

## 🛡️ 안정성 및 복원력

### Circuit Breaker 패턴
웹서버 연결 장애 시 자동으로 부하를 줄이고 복구를 시도합니다:

- **연속 실패 감지**: 3회 연속 실패 시 Circuit Open
- **자동 복구**: 30초 후 자동으로 재시도
- **Exponential Backoff**: 실패 시 대기 시간 점진적 증가
  - 1회 실패: 5초 대기
  - 2회 실패: 10초 대기
  - 3회 실패: 20초 대기 (Circuit Open)
  - 최대 대기 시간: 60초

### 타임아웃 최적화
빠른 실패와 복구를 위한 타임아웃 설정:

- **연결 타임아웃**: 3초 (기존 10초)
- **요청 타임아웃**: 5초 (기존 30초)
- 네트워크 장애 시 빠르게 감지하고 다음 시도로 전환

## 🐛 문제 해결

### 일반적인 문제들

1. **플러그인이 로드되지 않음**
   - Java 17이 설치되어 있는지 확인
   - 서버가 Spigot/Paper 1.20.4 이상인지 확인

2. **웹서버 통신 실패**
   - `server-code.yml` 파일의 서버 코드 확인
   - 웹서버가 정상 작동하는지 확인
   - 방화벽 설정 확인
   - 외부 IP 조회 실패 시 네트워크 연결 확인

3. **명령어 실행 실패**
   - 플레이어가 온라인인지 확인
   - 명령어 구문이 올바른지 확인

### 로그 확인

플러그인 로그는 서버 콘솔에서 `[GameBoyConnector]` 접두사로 확인할 수 있습니다.

## 📞 지원

- **개발자**: 수다
- **이메일**: sudapeopletv@gmail.com
- **웹사이트**: https://gameboy.kr

## 📄 라이선스

이 플러그인은 GameBoy.kr 전용으로 개발되었습니다.

---

**버전**: 1.3.2
**마지막 업데이트**: 2025-01-15
**호환성**: Java 17+ (권장 Java 21), Spigot/Paper 1.16~1.20.4

## 🆕 버전 1.3.x 업데이트 내용

### v1.3.2 (2025-01-15) - 웹서버 통신 안정성 개선
- ✅ **Circuit Breaker 패턴 추가**: 연속 실패 시 요청 스킵으로 서버 부하 방지
- ✅ **Exponential Backoff 적용**: 실패 시 대기 시간 점진적 증가 (5초 → 최대 60초)
- ✅ **타임아웃 최적화**: 연결 3초, 요청 5초로 단축하여 빠른 실패 처리
- ✅ **자동 복구 메커니즘**: 30초 후 자동으로 재시도 및 연결 복구
- ✅ **설정 파일 확장**: Circuit Breaker 관련 설정 추가

### v1.3.1 - 명령어 실행 개선
- ✅ **명령어 실행 방식 변경**: CommandResultCapture 제거, 콘솔 직접 실행으로 안정성 향상

### v1.3.0 - 명령어 결과 추적
- ✅ **명령어 실행 결과 캡처**: 실행 결과를 웹서버에 전송하여 추적 가능

## 🆕 버전 1.1.0 업데이트 내용

### 새로운 기능
- ✅ **외부 IP 자동 수집**: 다중 IP 서비스 지원으로 안정적인 외부 IP 감지
- ✅ **동적 프로토콜 버전**: 마인크래프트 버전에 따른 프로토콜 버전 자동 감지
- ✅ **실제 TPS 측정**: Paper 서버에서 실제 TPS 측정 지원
- ✅ **향상된 서버 정보**: 월드 정보, 플러그인 목록, 보안 정보 추가

### 개선사항
- ✅ **API 키 제거**: 서버 코드만으로 인증하는 간소화된 시스템
- ✅ **보안 강화**: 명령어 실행 로그 제거, IP 유효성 검사 추가
- ✅ **에러 처리 개선**: 네트워크 타임아웃 및 예외 처리 강화
- ✅ **웹 API 동기화**: 모든 엔드포인트 완전 동기화

### 호환성
- ✅ **Java 17**: 완전 호환
- ✅ **Minecraft 1.16~1.20.4**: 프로토콜 버전 자동 감지
- ✅ **Paper 서버**: TPS 측정 지원
- ✅ **Spigot 서버**: 기본 기능 지원