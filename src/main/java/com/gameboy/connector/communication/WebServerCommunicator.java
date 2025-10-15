package com.gameboy.connector.communication;

import com.gameboy.connector.GameboyConnector;
import com.gameboy.connector.models.PlayerInfo;
import com.gameboy.connector.models.ServerInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 웹서버 통신 클래스
 * GameBoy.kr 웹서버와 HTTP 통신을 담당합니다.
 */
public class WebServerCommunicator {

    private final String baseUrl;
    private final String serverCode;
    private final HttpClient httpClient;
    private final Gson gson;
    private final GameboyConnector plugin;

    // 로그 최적화를 위한 변수들
    private long lastPlayerListLog = 0;
    private long lastServerInfoLog = 0;
    private boolean firstConnection = true;
    private static final long LOG_INTERVAL = 60000; // 1분 (60초)

    // Circuit Breaker 패턴 - 연결 실패 시 부하 방지
    private int consecutiveFailures = 0;
    private long lastFailureTime = 0;
    private boolean circuitOpen = false;
    private static final int FAILURE_THRESHOLD = 3; // 3번 연속 실패 시 circuit open
    private static final long CIRCUIT_RESET_TIME = 30000; // 30초 후 재시도
    private static final long BACKOFF_BASE_TIME = 5000; // 5초 기본 백오프
    private static final long MAX_BACKOFF_TIME = 60000; // 최대 60초 백오프
    
    public WebServerCommunicator(String baseUrl, String serverCode) {
        this.baseUrl = baseUrl;
        this.serverCode = serverCode;
        this.plugin = GameboyConnector.getInstance();
        this.gson = new Gson();

        // HTTP 클라이언트 설정 - 타임아웃 단축으로 빠른 실패
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3)) // 10초 → 3초로 단축
                .build();
    }
    
    /**
     * Circuit Breaker 확인 - 연결 실패 시 요청 스킵
     */
    private boolean shouldSkipRequest() {
        long currentTime = System.currentTimeMillis();

        // Circuit이 열려있는 경우
        if (circuitOpen) {
            // 재시도 시간이 지났는지 확인
            if (currentTime - lastFailureTime >= CIRCUIT_RESET_TIME) {
                plugin.getPluginLogger().info("웹서버 연결 재시도 중...");
                circuitOpen = false;
                consecutiveFailures = 0;
                return false;
            }
            return true; // 아직 재시도 시간이 안됨
        }

        // Exponential backoff - 연속 실패 시 대기 시간 증가
        if (consecutiveFailures > 0) {
            long backoffTime = Math.min(
                BACKOFF_BASE_TIME * (long) Math.pow(2, consecutiveFailures - 1),
                MAX_BACKOFF_TIME
            );
            if (currentTime - lastFailureTime < backoffTime) {
                return true; // 백오프 시간 동안 요청 스킵
            }
        }

        return false;
    }

    /**
     * 연결 성공 처리
     */
    private void recordSuccess() {
        if (consecutiveFailures > 0 || circuitOpen) {
            plugin.getPluginLogger().info("웹서버 연결 복구됨");
        }
        consecutiveFailures = 0;
        circuitOpen = false;
    }

    /**
     * 연결 실패 처리
     */
    private void recordFailure() {
        consecutiveFailures++;
        lastFailureTime = System.currentTimeMillis();

        if (consecutiveFailures >= FAILURE_THRESHOLD && !circuitOpen) {
            circuitOpen = true;
            plugin.getPluginLogger().warning(
                "웹서버 연결 실패가 " + FAILURE_THRESHOLD + "회 연속 발생했습니다. " +
                (CIRCUIT_RESET_TIME / 1000) + "초 후 재시도합니다."
            );
        }
    }

    /**
     * 플레이어 목록을 웹서버로 전송
     */
    public void updatePlayerList(List<PlayerInfo> players) {
        // Circuit Breaker 확인
        if (shouldSkipRequest()) {
            return;
        }

        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("server_code", serverCode);
            requestData.addProperty("timestamp", Instant.now().toString());
            requestData.add("online_players", gson.toJsonTree(players));
            requestData.addProperty("total_players", players.size());

            sendRequest("/plugin-player-list.php", requestData);
            recordSuccess();

        } catch (Exception e) {
            recordFailure();
            plugin.getPluginLogger().severe("플레이어 목록 전송 실패: " + e.getMessage());
        }
    }
    
    /**
     * 서버 정보를 웹서버로 전송
     */
    public void sendServerInfo(ServerInfo serverInfo) {
        // Circuit Breaker 확인
        if (shouldSkipRequest()) {
            return;
        }

        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("server_code", serverCode);
            requestData.addProperty("timestamp", Instant.now().toString());
            requestData.add("server_info", gson.toJsonTree(serverInfo));

            sendRequest("/plugin-server-info.php", requestData);
            recordSuccess();

        } catch (Exception e) {
            recordFailure();
            plugin.getPluginLogger().severe("서버 정보 전송 실패: " + e.getMessage());
        }
    }
    
    /**
     * 웹서버 연결 테스트
     */
    public boolean testConnection() {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("server_code", serverCode);
            requestData.addProperty("timestamp", Instant.now().toString());
            requestData.addProperty("test", true);
            
            String response = sendRequest("/plugin-test.php", requestData);
            JsonObject responseJson = gson.fromJson(response, JsonObject.class);
            
            return responseJson.get("status").getAsString().equals("success");
            
        } catch (Exception e) {
            plugin.getPluginLogger().severe("웹서버 연결 테스트 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 서버 종료 알림을 웹서버로 전송
     */
    public void sendServerShutdown() {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("server_code", serverCode);
            requestData.addProperty("timestamp", Instant.now().toString());
            requestData.addProperty("action", "server_shutdown");
            
            sendRequest("/server-shutdown.php", requestData);
            
        } catch (Exception e) {
            plugin.getPluginLogger().severe("서버 종료 알림 전송 실패: " + e.getMessage());
        }
    }
    
    /**
     * 명령어 실행 결과를 웹서버로 전송
     */
    public void sendCommandResult(String command, String playerName, boolean success, String output, long executionTime) {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("server_code", serverCode);
            requestData.addProperty("timestamp", Instant.now().toString());
            requestData.addProperty("original_command", command);
            requestData.addProperty("executed_command", command.replace("<player>", playerName));
            requestData.addProperty("player_name", playerName);
            requestData.addProperty("success", success);
            requestData.addProperty("output", output);
            requestData.addProperty("execution_time", executionTime);
            
            sendRequest("/command-result.php", requestData);
            
        } catch (Exception e) {
            plugin.getPluginLogger().severe("명령어 실행 결과 전송 실패: " + e.getMessage());
        }
    }
    
    /**
     * HTTP 요청 전송
     */
    private String sendRequest(String endpoint, JsonObject data) throws IOException, InterruptedException {
        String url = baseUrl + endpoint;
        String jsonData = gson.toJson(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Server-Code", serverCode)
                .header("User-Agent", "GameboyConnector/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .timeout(Duration.ofSeconds(5)) // 30초 → 5초로 단축
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        long currentTime = System.currentTimeMillis();
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // 첫 연결 시에만 성공 로그 출력 (API 경로 숨김)
            if (firstConnection) {
                plugin.getPluginLogger().info("웹서버 연결 성공");
                firstConnection = false;
            }
            // 주기적 통신 로그는 제거 (보안상 API 경로 노출 방지)
        } else {
            plugin.getPluginLogger().warning("웹서버 통신 실패 (상태: " + response.statusCode() + ")");
        }
        
        return response.body();
    }
    
    /**
     * 주기적 로그 출력 여부 확인
     */
    private boolean shouldLogPeriodic(String endpoint, long currentTime) {
        if (endpoint.contains("player-list")) {
            if (currentTime - lastPlayerListLog >= LOG_INTERVAL) {
                lastPlayerListLog = currentTime;
                return true;
            }
        } else if (endpoint.contains("server-info")) {
            if (currentTime - lastServerInfoLog >= LOG_INTERVAL) {
                lastServerInfoLog = currentTime;
                return true;
            }
        }
        return false;
    }
    
    /**
     * 웹서버에서 명령어 실행 요청 받기
     */
    public void handleCommandRequest(String command, String playerName) {
        plugin.executeWebCommand(command, playerName);
    }
    
    /**
     * 대기 중인 명령어들을 웹서버에서 가져와서 실행
     */
    public void pollAndExecuteCommands() {
        // Circuit Breaker 확인
        if (shouldSkipRequest()) {
            return;
        }

        try {
            // GET 요청으로 대기 중인 명령어 조회
            String url = baseUrl + "/pending-commands.php?server_code=" + serverCode;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-Server-Code", serverCode)
                    .header("User-Agent", "GameboyConnector/1.0")
                    .GET()
                    .timeout(Duration.ofSeconds(5)) // 30초 → 5초로 단축
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                recordSuccess(); // 연결 성공
                JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);

                if (responseJson.get("status").getAsString().equals("success")) {
                    var pendingCommands = responseJson.getAsJsonArray("pending_commands");

                    for (var commandElement : pendingCommands) {
                        JsonObject command = commandElement.getAsJsonObject();

                        int queueId = command.get("id").getAsInt();
                        String playerName = command.get("player_name").getAsString();
                        String commandString = command.get("command_string").getAsString();

                        // 명령어 상태를 processing으로 변경
                        updateCommandStatus(queueId, "processing", null);

                        // 특수 명령어 처리
                        if (commandString.equals("REFRESH_PLAYERS")) {
                            // 플레이어 목록 즉시 갱신
                            try {
                                List<PlayerInfo> players = plugin.getServer().getOnlinePlayers().stream()
                                    .map(player -> {
                                        PlayerInfo playerInfo = new PlayerInfo();
                                        playerInfo.setPlayerId(player.getName());
                                        playerInfo.setPlayerUuid(player.getUniqueId().toString());
                                        return playerInfo;
                                    })
                                    .toList();

                                updatePlayerList(players);
                                updateCommandStatus(queueId, "completed", null);
                                plugin.getPluginLogger().info("플레이어 목록 즉시 갱신 완료");
                                continue;
                            } catch (Exception e) {
                                plugin.getPluginLogger().severe("플레이어 ���록 갱신 실패: " + e.getMessage());
                                updateCommandStatus(queueId, "failed", null);
                                continue;
                            }
                        }

                        // 일반 명령어 실행 (메인 스레드에서)
                        GameboyConnector.CommandExecutionResult cmdResult = null;
                        try {
                            // CompletableFuture를 사용해서 메인 스레드에서 실행하고 결과를 기다림
                            CompletableFuture<GameboyConnector.CommandExecutionResult> future = new CompletableFuture<>();

                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                try {
                                    GameboyConnector.CommandExecutionResult result = plugin.executeWebCommand(commandString, playerName);
                                    future.complete(result);
                                } catch (Exception e) {
                                    plugin.getPluginLogger().severe("명령어 실행 중 오류: " + e.getMessage());
                                    GameboyConnector.CommandExecutionResult errorResult = new GameboyConnector.CommandExecutionResult();
                                    errorResult.setSuccess(false);
                                    errorResult.setOutput("오류: " + e.getMessage());
                                    future.complete(errorResult);
                                }
                            });

                            // 최대 5초 대기
                            cmdResult = future.get(5, TimeUnit.SECONDS);

                        } catch (Exception e) {
                            plugin.getPluginLogger().severe("명령어 실행 실패: " + e.getMessage());
                            cmdResult = new GameboyConnector.CommandExecutionResult();
                            cmdResult.setSuccess(false);
                            cmdResult.setOutput("타임아웃 또는 오류: " + e.getMessage());
                        }

                        // 실행 결과에 따라 상태 업데이트
                        JsonObject executionResult = new JsonObject();
                        executionResult.addProperty("success", cmdResult.isSuccess());
                        executionResult.addProperty("player_name", playerName);
                        executionResult.addProperty("command", commandString);
                        executionResult.addProperty("executed_command", cmdResult.getExecutedCommand());
                        executionResult.addProperty("output", cmdResult.getOutput());
                        executionResult.addProperty("execution_time", Instant.now().toString());

                        String status = cmdResult.isSuccess() ? "completed" : "failed";
                        updateCommandStatus(queueId, status, executionResult);
                    }
                }
            } else {
                recordFailure(); // HTTP 오류
                plugin.getPluginLogger().warning("명령어 폴링 실패: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            recordFailure(); // 네트워크 오류
            plugin.getPluginLogger().severe("명령어 폴링 실패: " + e.getMessage());
        }
    }
    
    /**
     * 명령어 상태 업데이트
     */
    private void updateCommandStatus(int queueId, String status, JsonObject executionResult) {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("queue_id", queueId);
            requestData.addProperty("status", status);
            if (executionResult != null) {
                requestData.add("execution_result", executionResult);
            }
            
            sendRequest("/pending-commands.php", requestData);
            
        } catch (Exception e) {
            plugin.getPluginLogger().severe("명령어 상태 업데이트 실패: " + e.getMessage());
        }
    }
    
    // Getter 메서드들
    public String getBaseUrl() {
        return baseUrl;
    }
    
    
    public String getServerCode() {
        return serverCode;
    }
}