package com.gameboy.connector.models;

import java.util.List;

/**
 * 서버 정보 모델 클래스
 * 웹서버로 전송할 서버 정보를 담습니다.
 */
public class ServerInfo {
    
    // 서버 식별 정보
    private String serverCode;
    private String serverName;
    private String internalIp;
    private String externalIp;
    
    // 서버 기본 정보
    private int serverPort;
    private String motd;
    private String version;
    private int protocolVersion;
    private int maxPlayers;
    private int onlinePlayers;
    
    // 서버 성능 정보
    private double tps;
    private long serverUptime;
    private long usedMemory;
    private long totalMemory;
    private double memoryPercentage;
    
    // 시스템 정보
    private String javaVersion;
    private String osInfo;
    
    // 서버 설정 정보
    private String gamemode;
    private String difficulty;
    private boolean pvpEnabled;
    private boolean whitelistEnabled;
    
    // 월드 정보
    private String worldName;
    private long worldSize;
    private int chunksLoaded;
    
    // 플러그인 정보
    private List<PluginInfo> plugins;
    
    // 보안 정보
    private int opCount;
    
    public ServerInfo() {
        // 기본 생성자
    }
    
    // Getter 메서드들
    public String getServerCode() {
        return serverCode;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public String getInternalIp() {
        return internalIp;
    }
    
    public String getExternalIp() {
        return externalIp;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public String getMotd() {
        return motd;
    }
    
    public String getVersion() {
        return version;
    }
    
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public int getOnlinePlayers() {
        return onlinePlayers;
    }
    
    public double getTps() {
        return tps;
    }
    
    public long getServerUptime() {
        return serverUptime;
    }
    
    public long getUsedMemory() {
        return usedMemory;
    }
    
    public long getTotalMemory() {
        return totalMemory;
    }
    
    public double getMemoryPercentage() {
        return memoryPercentage;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }
    
    public String getOsInfo() {
        return osInfo;
    }
    
    public String getGamemode() {
        return gamemode;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }
    
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public long getWorldSize() {
        return worldSize;
    }
    
    public int getChunksLoaded() {
        return chunksLoaded;
    }
    
    public List<PluginInfo> getPlugins() {
        return plugins;
    }
    
    public int getOpCount() {
        return opCount;
    }
    
    // Setter 메서드들
    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public void setInternalIp(String internalIp) {
        this.internalIp = internalIp;
    }
    
    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }
    
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public void setMotd(String motd) {
        this.motd = motd;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
    
    public void setTps(double tps) {
        this.tps = tps;
    }
    
    public void setServerUptime(long serverUptime) {
        this.serverUptime = serverUptime;
    }
    
    public void setMemoryUsage(long usedMemory, long totalMemory) {
        this.usedMemory = usedMemory;
        this.totalMemory = totalMemory;
        this.memoryPercentage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0;
    }
    
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
    
    public void setOsInfo(String osInfo) {
        this.osInfo = osInfo;
    }
    
    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }
    
    public void setWhitelistEnabled(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }
    
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
    
    public void setWorldSize(long worldSize) {
        this.worldSize = worldSize;
    }
    
    public void setChunksLoaded(int chunksLoaded) {
        this.chunksLoaded = chunksLoaded;
    }
    
    public void setPlugins(List<PluginInfo> plugins) {
        this.plugins = plugins;
    }
    
    public void setOpCount(int opCount) {
        this.opCount = opCount;
    }
    
    @Override
    public String toString() {
        return "ServerInfo{" +
                "serverCode='" + serverCode + '\'' +
                ", serverName='" + serverName + '\'' +
                ", internalIp='" + internalIp + '\'' +
                ", serverPort=" + serverPort +
                ", motd='" + motd + '\'' +
                ", version='" + version + '\'' +
                ", protocolVersion=" + protocolVersion +
                ", maxPlayers=" + maxPlayers +
                ", onlinePlayers=" + onlinePlayers +
                ", tps=" + tps +
                ", serverUptime=" + serverUptime +
                ", usedMemory=" + usedMemory +
                ", totalMemory=" + totalMemory +
                ", memoryPercentage=" + memoryPercentage +
                ", javaVersion='" + javaVersion + '\'' +
                ", osInfo='" + osInfo + '\'' +
                ", gamemode='" + gamemode + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", pvpEnabled=" + pvpEnabled +
                ", whitelistEnabled=" + whitelistEnabled +
                ", worldName='" + worldName + '\'' +
                ", worldSize=" + worldSize +
                ", chunksLoaded=" + chunksLoaded +
                ", plugins=" + plugins +
                ", opCount=" + opCount +
                '}';
    }
}