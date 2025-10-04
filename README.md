# GameBoy Connector Plugin

GameBoy.kr 웹서버와 통신하는 마인크래프트 플러그인입니다.

## 📋 개요

이 플러그인은 GameBoy.kr 웹사이트와 실시간으로 통신하여 다음 기능을 제공합니다:

- **플레이어 목록 제공**: 웹서버에서 접속한 플레이어 목록 조회
- **명령어 실행**: 웹서버에서 마인크래프트 명령어 실행
- **서버 정보 수집**: 서버 상태, 성능, 설정 정보 전송
- **대체문자 처리**: `<player>` 등의 대체문자를 실제 값으로 변환

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
3. API 키 설정 (필요시)

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

# 웹서버 통신 설정
web_server:
  api_key: "your-api-key-here"
  update_interval: 60

# 서버 정보 수집 설정
data_collection:
  collect_server_info: true
  collect_player_list: true
  collect_performance: true
  collect_plugins: true

# 명령어 실행 설정
command_execution:
  max_command_length: 1000
  allowed_commands:
    - "roulette_reward"
    - "pen"
    - "give"
    - "tp"
    - "gamemode"
    - "weather"
    - "time"
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

# 웹서버 연동 설정
web_server:
  api_key: "your-api-key-here"
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

- `/gameboy help` - 도움말 표시
- `/gameboy info` - 서버 정보 표시
- `/gameboy reload` - 설정 다시 로드
- `/gameboy test` - 웹서버 연결 테스트

**권한**: `gameboy.admin` (기본적으로 OP만 사용 가능)

## 🔌 API 통신

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
    "server_port": 25565,
    "motd": "Welcome to My Server!",
    "version": "1.20.4",
    "max_players": 64,
    "online_players": 15,
    "tps": 20.0,
    "memory_usage": {
      "used": "2.5GB",
      "total": "4GB",
      "percentage": 62.5
    },
    "java_version": "21.0.1",
    "os_info": "Linux Ubuntu 22.04 LTS"
  }
}
```

### 명령어 실행

웹서버에서 명령어 실행 요청을 받으면 플러그인이 처리합니다.

**요청 예시**:
```json
{
  "server_code": "GB-A1B2-C3D4-E5F6",
  "command": "roulette_reward <player>",
  "player_name": "플레이어1",
  "executor": "web_admin",
  "timestamp": "2024-01-15T14:30:00Z"
}
```

**실행 결과**:
- `<player>` → `플레이어1`로 변환
- `roulette_reward 플레이어1` 명령어 실행

## 🔐 보안

- **API URL 보안**: API URL은 플러그인 내부 코드에서만 관리
- **서버 코드 시스템**: 고유한 서버 식별 코드로 서버 구분
- **인증**: API 키 기반 인증
- **명령어 제한**: 허용된 명령어만 실행 가능

## 🐛 문제 해결

### 일반적인 문제들

1. **플러그인이 로드되지 않음**
   - Java 17이 설치되어 있는지 확인
   - 서버가 Spigot/Paper 1.20.4 이상인지 확인

2. **웹서버 통신 실패**
   - `server-code.yml` 파일의 API 키 확인
   - 웹서버가 정상 작동하는지 확인
   - 방화벽 설정 확인

3. **명령어 실행 실패**
   - `config.yml`의 `allowed_commands` 목록 확인
   - 플레이어가 온라인인지 확인

### 로그 확인

플러그인 로그는 서버 콘솔에서 `[GameBoyConnector]` 접두사로 확인할 수 있습니다.

## 📞 지원

- **개발자**: GameBoy.kr 개발팀
- **이메일**: sudapeopletv@gmail.com
- **웹사이트**: https://gameboy.kr

## 📄 라이선스

이 플러그인은 GameBoy.kr 전용으로 개발되었습니다.

---

**버전**: 1.0.0  
**마지막 업데이트**: 2024-01-15  
**호환성**: Java 17, Spigot/Paper 1.20.4+