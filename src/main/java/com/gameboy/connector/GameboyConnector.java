package com.gameboy.connector;

import com.gameboy.connector.commands.GameboyCommand;
import com.gameboy.connector.communication.WebServerCommunicator;
import com.gameboy.connector.config.ServerCodeConfig;
import com.gameboy.connector.listeners.PlayerEventListener;
import com.gameboy.connector.models.PlayerInfo;
import com.gameboy.connector.models.ServerInfo;
import com.gameboy.connector.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * GameBoy Connector 플러그인 메인 클래스
 * GameBoy.kr 웹서버와 통신하는 마인크래프트 플러그인
 */
public class GameboyConnector extends JavaPlugin {
    
    private static GameboyConnector instance;
    private WebServerCommunicator communicator;
    private ServerCodeConfig serverConfig;
    private Logger logger;
    private boolean isEnabled = false;
    private long serverStartTime;
    
    // 플러그인 상수
    private static final String SERVER_CODE_FILE = "server-code.yml";
    private static final String API_BASE_URL = "https://gameboy.kr/api/plugins/gameboy-connector";
    
    @Override
    public void onEnable() {
        instance = this;
        logger = new Logger(this);
        serverStartTime = System.currentTimeMillis();
        
        try {
            // 플러그인 초기화
            initializePlugin();
            
            // 서버 코드 시스템 초기화
            initializeServerCode();
            
            // 웹서버 통신자 초기화
            initializeCommunicator();
            
            // 이벤트 리스너 등록
            registerEventListeners();
            
            // 명령어 등록
            registerCommands();
            
            // 스케줄러 시작
            startSchedulers();
            
            isEnabled = true;
            logger.info("GameboyConnector 플러그인이 활성화되었습니다. (Java 21, SQL/DB 없음)");
            logger.info("서버 코드: " + serverConfig.getServerCode());
            
        } catch (Exception e) {
            logger.severe("플러그인 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (isEnabled) {
            logger.info("GameboyConnector 플러그인이 비활성화되었습니다.");
            
            // 웹서버에 서버 종료 알림
            if (communicator != null) {
                communicator.sendServerShutdown();
            }
        }
    }
    
    /**
     * 플러그인 초기화
     */
    private void initializePlugin() {
        // config.yml 저장
        saveDefaultConfig();
        
        // 데이터 폴더 생성
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // 설정 로드
        loadPluginConfig();
        
        logger.info("플러그인 초기화 완료");
    }
    
    /**
     * 플러그인 설정 로드
     */
    public void loadPluginConfig() {
        try {
            // config.yml에서 설정값 읽기
            boolean debug = getConfig().getBoolean("plugin.debug", false);
            boolean updateCheck = getConfig().getBoolean("plugin.update_check", true);
            int updateInterval = getConfig().getInt("web_server.update_interval", 60);
            boolean collectServerInfo = getConfig().getBoolean("data_collection.collect_server_info", true);
            boolean collectPlayerList = getConfig().getBoolean("data_collection.collect_player_list", true);
            boolean collectPerformance = getConfig().getBoolean("data_collection.collect_performance", true);
            boolean collectPlugins = getConfig().getBoolean("data_collection.collect_plugins", true);
            int maxCommandLength = getConfig().getInt("command_execution.max_command_length", 1000);
            boolean logCommands = getConfig().getBoolean("logging.log_commands", true);
            boolean logErrors = getConfig().getBoolean("logging.log_errors", true);
            boolean logPerformance = getConfig().getBoolean("logging.log_performance", false);
            
            // 설정값 적용 (디버그 모드는 로거에서 자동 처리)
            
            logger.info("플러그인 설정 로드 완료:");
            logger.info("- 디버그 모드: " + debug);
            logger.info("- 업데이트 체크: " + updateCheck);
            logger.info("- 업데이트 간격: " + updateInterval + "초");
            logger.info("- 서버 정보 수집: " + collectServerInfo);
            logger.info("- 플레이어 목록 수집: " + collectPlayerList);
            logger.info("- 성능 데이터 수집: " + collectPerformance);
            logger.info("- 플러그인 목록 수집: " + collectPlugins);
            logger.info("- 최대 명령어 길이: " + maxCommandLength);
            logger.info("- 명령어 로깅: " + logCommands);
            logger.info("- 에러 로깅: " + logErrors);
            logger.info("- 성능 로깅: " + logPerformance);
            
        } catch (Exception e) {
            logger.severe("플러그인 설정 로드 실패: " + e.getMessage());
        }
    }
    
    /**
     * 서버 코드 시스템 초기화
     */
    private void initializeServerCode() {
        File serverCodeFile = new File(getDataFolder(), SERVER_CODE_FILE);
        
        if (!serverCodeFile.exists()) {
            // 서버 코드 파일이 없으면 새로 생성
            createServerCodeFile(serverCodeFile);
        }
        
        // 서버 코드 설정 로드
        serverConfig = loadServerCodeConfig();
        
        logger.info("서버 코드 시스템 초기화 완료: " + serverConfig.getServerCode());
    }
    
    /**
     * 서버 코드 파일 생성
     */
    private void createServerCodeFile(File file) {
        try {
            getDataFolder().mkdirs();
            
            // 고유 서버 코드 생성 (GB-XXXX-XXXX-XXXX 형식)
            String serverCode = generateServerCode();
            String serverName = getServer().getName();
            String currentTime = Instant.now().toString();
            
            // YAML 파일 내용 생성 (API URL은 보안상 제외)
            StringBuilder yamlContent = new StringBuilder();
            yamlContent.append("# GameBoy Connector 서버 코드 설정\n");
            yamlContent.append("server:\n");
            yamlContent.append("  code: \"").append(serverCode).append("\"\n");
            yamlContent.append("  name: \"").append(serverName).append("\"\n");
            yamlContent.append("  description: \"자동 생성된 서버\"\n");
            yamlContent.append("  created_at: \"").append(currentTime).append("\"\n");
            yamlContent.append("  last_updated: \"").append(currentTime).append("\"\n\n");
            yamlContent.append("# 웹서버 연동 설정 (보안상 API URL은 플러그인 내부 코드에서만 관리)\n");
            yamlContent.append("web_server:\n");
            yamlContent.append("  update_interval: 60\n\n");
            yamlContent.append("# 서버 정보 수집 설정\n");
            yamlContent.append("data_collection:\n");
            yamlContent.append("  collect_server_info: true\n");
            yamlContent.append("  collect_player_list: true\n");
            yamlContent.append("  collect_performance: true\n");
            yamlContent.append("  collect_plugins: true\n");
            
            // 파일에 쓰기
            Files.write(file.toPath(), yamlContent.toString().getBytes(StandardCharsets.UTF_8));
            
            logger.info("서버 코드가 생성되었습니다: " + serverCode);
            logger.info("웹서버에서 이 코드를 사용하여 서버를 등록하세요.");
            
        } catch (IOException e) {
            logger.severe("서버 코드 파일 생성 실패: " + e.getMessage());
            throw new RuntimeException("서버 코드 파일 생성 실패", e);
        }
    }
    
    /**
     * 고유 서버 코드 생성
     */
    private String generateServerCode() {
        // UUID를 사용하여 고유 코드 생성
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "GB-" + uuid.substring(0, 4) + "-" + uuid.substring(4, 8) + "-" + uuid.substring(8, 12);
    }
    
    /**
     * 서버 코드 설정 로드
     */
    private ServerCodeConfig loadServerCodeConfig() {
        File serverCodeFile = new File(getDataFolder(), SERVER_CODE_FILE);
        ServerCodeConfig config = new ServerCodeConfig();
        
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new FileInputStream(serverCodeFile));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> serverData = (Map<String, Object>) data.get("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> webServerData = (Map<String, Object>) data.get("web_server");
            
            config.setServerCode((String) serverData.get("code"));
            config.setServerName((String) serverData.get("name"));
            config.setUpdateInterval((Integer) webServerData.get("update_interval"));
            
        } catch (Exception e) {
            logger.severe("서버 코드 설정 로드 실패: " + e.getMessage());
            // 기본값 설정
            config.setServerCode("GB-UNKNOWN");
            config.setUpdateInterval(60);
        }
        
        return config;
    }
    
    /**
     * 웹서버 통신자 초기화
     */
    private void initializeCommunicator() {
        communicator = new WebServerCommunicator(
            API_BASE_URL,
            serverConfig.getServerCode()
        );
        
        logger.info("웹서버 통신자 초기화 완료");
    }
    
    /**
     * 이벤트 리스너 등록
     */
    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        logger.info("이벤트 리스너 등록 완료");
    }
    
    /**
     * 명령어 등록
     */
    private void registerCommands() {
        // 기본 명령어는 plugin.yml에서 자동 등록됨
        logger.info("명령어 등록 완료");
    }
    
    /**
     * 스케줄러 시작
     */
    private void startSchedulers() {
        // 플레이어 목록 업데이트 (10초마다)
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                List<PlayerInfo> players = collectPlayerList();
                communicator.updatePlayerList(players);
            } catch (Exception e) {
                logger.severe("플레이어 목록 업데이트 실패: " + e.getMessage());
            }
        }, 0L, 200L); // 10초 = 200 ticks
        
        // 서버 정보 수집 및 전송 (1분마다)
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                collectAndSendServerInfo();
            } catch (Exception e) {
                logger.severe("서버 정보 수집 실패: " + e.getMessage());
            }
        }, 0L, 1200L); // 1분 = 1200 ticks
        
        // 명령어 폴링 및 실행 (5초마다)
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                communicator.pollAndExecuteCommands();
            } catch (Exception e) {
                logger.severe("명령어 폴링 실패: " + e.getMessage());
            }
        }, 100L, 100L); // 5초 = 100 ticks (첫 실행은 5초 후)
        
        logger.info("스케줄러 시작 완료");
    }
    
    /**
     * 플레이어 목록 수집
     */
    private List<PlayerInfo> collectPlayerList() {
        return getServer().getOnlinePlayers().stream()
            .map(player -> {
                PlayerInfo playerInfo = new PlayerInfo();
                playerInfo.setPlayerId(player.getName());
                playerInfo.setPlayerUuid(player.getUniqueId().toString());
                return playerInfo;
            })
            .toList();
    }
    
    /**
     * 서버 정보 수집 및 전송
     */
    private void collectAndSendServerInfo() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                ServerInfo serverInfo = new ServerInfo();
                
                // 서버 코드 설정 로드
                serverInfo.setServerCode(serverConfig.getServerCode());
                serverInfo.setServerName(serverConfig.getServerName());
                
                // 내부 IP 및 외부 IP 수집
                String internalIp = getInternalIp();
                String externalIp = getExternalIp();
                serverInfo.setInternalIp(internalIp);
                serverInfo.setExternalIp(externalIp);
                
                // 서버 기본 정보
                serverInfo.setServerPort(getServer().getPort());
                serverInfo.setMotd(getServer().getMotd());
                serverInfo.setVersion(getServer().getVersion());
                serverInfo.setProtocolVersion(getProtocolVersion()); // 동적 프로토콜 버전 감지
                serverInfo.setMaxPlayers(getServer().getMaxPlayers());
                serverInfo.setOnlinePlayers(getServer().getOnlinePlayers().size());
                
                // 서버 성능 정보
                serverInfo.setTps(getServerTps()); // 실제 TPS 측정
                serverInfo.setServerUptime(System.currentTimeMillis() - getServerStartTime());
                
                // 메모리 사용량
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                serverInfo.setMemoryUsage(usedMemory, totalMemory);
                
                // Java 버전
                serverInfo.setJavaVersion(System.getProperty("java.version"));
                
                // OS 정보
                serverInfo.setOsInfo(System.getProperty("os.name") + " " + System.getProperty("os.version"));
                
                // 서버 설정 정보
                serverInfo.setGamemode(getServer().getDefaultGameMode().name().toLowerCase());
                serverInfo.setDifficulty(getServer().getWorlds().get(0).getDifficulty().name().toLowerCase());
                serverInfo.setPvpEnabled(getServer().getWorlds().get(0).getPVP());
                serverInfo.setWhitelistEnabled(getServer().hasWhitelist());
                
                // 월드 정보
                World mainWorld = getServer().getWorlds().get(0);
                serverInfo.setWorldName(mainWorld.getName());
                serverInfo.setWorldSize(getWorldSize(mainWorld));
                serverInfo.setChunksLoaded(mainWorld.getLoadedChunks().length);
                
                // 플러그인 정보
                List<com.gameboy.connector.models.PluginInfo> plugins = new ArrayList<>();
                for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
                    com.gameboy.connector.models.PluginInfo pluginInfo = new com.gameboy.connector.models.PluginInfo();
                    pluginInfo.setName(plugin.getName());
                    pluginInfo.setVersion(plugin.getDescription().getVersion());
                    pluginInfo.setEnabled(plugin.isEnabled());
                    plugins.add(pluginInfo);
                }
                serverInfo.setPlugins(plugins);
                
                // 보안 정보
                serverInfo.setOpCount(getServer().getOperators().size());
                
                // 웹서버로 서버 정보 전송
                communicator.sendServerInfo(serverInfo);
                
            } catch (Exception e) {
                logger.severe("서버 정보 수집 중 오류 발생: " + e.getMessage());
            }
        });
    }
    
    /**
     * 내부 IP 조회
     */
    private String getInternalIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * 외부 IP 조회 (필수 기능)
     */
    private String getExternalIp() {
        try {
            // 여러 IP 확인 서비스 시도
            String[] ipServices = {
                "https://api.ipify.org",
                "https://ipv4.icanhazip.com",
                "https://checkip.amazonaws.com",
                "https://ifconfig.me/ip"
            };
            
            for (String service : ipServices) {
                try {
                    java.net.URL url = new java.net.URL(service);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream())
                    );
                    String ip = reader.readLine().trim();
                    reader.close();
                    
                    // IP 주소 유효성 검사
                    if (isValidIpAddress(ip) && !isPrivateIp(ip)) {
                        return ip;
                    }
                } catch (Exception e) {
                    logger.debug("IP 서비스 실패: " + service + " - " + e.getMessage());
                    continue;
                }
            }
            
            logger.severe("외부 IP 조회 실패: 모든 서비스에서 실패");
            return "Unknown";
            
        } catch (Exception e) {
            logger.severe("외부 IP 조회 중 오류: " + e.getMessage());
            return "Unknown";
        }
    }
    
    /**
     * IP 주소 유효성 검사
     */
    private boolean isValidIpAddress(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) return false;
            
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 사설 IP 주소 확인
     */
    private boolean isPrivateIp(String ip) {
        try {
            String[] parts = ip.split("\\.");
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            
            // 사설 IP 대역 확인
            return (first == 10) || 
                   (first == 172 && second >= 16 && second <= 31) ||
                   (first == 192 && second == 168) ||
                   (first == 127); // 루프백
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 월드 크기 계산
     */
    private long getWorldSize(World world) {
        File worldFolder = world.getWorldFolder();
        return calculateFolderSize(worldFolder);
    }
    
    /**
     * 폴더 크기 계산
     */
    private long calculateFolderSize(File folder) {
        long size = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += calculateFolderSize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * 웹서버에서 명령어 실행 요청 받기 (대체문자 처리 포함)
     */
    public boolean executeWebCommand(String command, String playerName) {
        try {
            // <player> 대체문자를 실제 플레이어명으로 변환
            String executedCommand = command.replace("<player>", playerName);
            
            // 마인크래프트 명령어 그대로 실행
            getServer().dispatchCommand(getServer().getConsoleSender(), executedCommand);
            // 보안상 명령어 실행 로그 제거
            
            return true;
            
        } catch (Exception e) {
            logger.severe("명령어 실행 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 명령어 실행 처리
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return new GameboyCommand(this).onCommand(sender, command, label, args);
    }
    
    // Getter 메서드들
    public static GameboyConnector getInstance() {
        return instance;
    }
    
    public WebServerCommunicator getCommunicator() {
        return communicator;
    }
    
    public ServerCodeConfig getServerConfig() {
        return serverConfig;
    }
    
    public Logger getPluginLogger() {
        return logger;
    }
    
    public boolean isPluginEnabled() {
        return isEnabled;
    }
    
    /**
     * 서버 시작 시간 반환
     */
    public long getServerStartTime() {
        return serverStartTime;
    }
    
    /**
     * 프로토콜 버전 동적 감지
     */
    private int getProtocolVersion() {
        String version = getServer().getVersion();
        try {
            // 버전 문자열에서 숫자 추출 (예: "git-Paper-1.20.4-R0.1-SNAPSHOT")
            if (version.contains("1.20.4")) return 765;
            if (version.contains("1.20.3")) return 765;
            if (version.contains("1.20.2")) return 764;
            if (version.contains("1.20.1")) return 763;
            if (version.contains("1.20")) return 763;
            if (version.contains("1.19.4")) return 762;
            if (version.contains("1.19.3")) return 761;
            if (version.contains("1.19.2")) return 760;
            if (version.contains("1.19.1")) return 759;
            if (version.contains("1.19")) return 759;
            if (version.contains("1.18.2")) return 758;
            if (version.contains("1.18.1")) return 757;
            if (version.contains("1.18")) return 757;
            if (version.contains("1.17.1")) return 756;
            if (version.contains("1.17")) return 755;
            if (version.contains("1.16.5")) return 754;
            if (version.contains("1.16.4")) return 754;
            if (version.contains("1.16.3")) return 753;
            if (version.contains("1.16.2")) return 751;
            if (version.contains("1.16.1")) return 736;
            if (version.contains("1.16")) return 735;
            
            // 기본값 (1.20.4)
            return 765;
        } catch (Exception e) {
            logger.warning("프로토콜 버전 감지 실패, 기본값 사용: " + e.getMessage());
            return 765; // 1.20.4 기본값
        }
    }
    
    /**
     * 서버 TPS 측정 (Paper API 지원)
     */
    private double getServerTps() {
        try {
            // Paper 서버인지 확인
            if (getServer().getName().contains("Paper")) {
                // Paper API를 사용한 실제 TPS 측정
                try {
                    // Reflection을 사용하여 Paper의 TPS 메트릭스에 접근
                    Class<?> serverClass = getServer().getClass();
                    java.lang.reflect.Method getTpsMethod = serverClass.getMethod("getTPS");
                    double[] tps = (double[]) getTpsMethod.invoke(getServer());
                    if (tps != null && tps.length > 0) {
                        return tps[0]; // 1분 평균 TPS
                    }
                } catch (Exception e) {
                    logger.debug("Paper TPS API 접근 실패, 기본값 사용: " + e.getMessage());
                }
            }
            
            // 기본 TPS (20.0)
            return 20.0;
        } catch (Exception e) {
            logger.warning("TPS 측정 실패, 기본값 사용: " + e.getMessage());
            return 20.0;
        }
    }
}