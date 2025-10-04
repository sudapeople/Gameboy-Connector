package com.gameboy.connector.config;

/**
 * 서버 코드 설정 클래스
 * server-code.yml 파일의 설정을 관리합니다.
 */
public class ServerCodeConfig {
    
    private String serverCode;
    private String serverName;
    private int updateInterval;
    
    public ServerCodeConfig() {
        // 기본값 설정
        this.serverCode = "GB-UNKNOWN";
        this.serverName = "Unknown Server";
        this.updateInterval = 60;
    }
    
    // Getter 메서드들
    public String getServerCode() {
        return serverCode;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    
    public int getUpdateInterval() {
        return updateInterval;
    }
    
    // Setter 메서드들
    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }
    
    @Override
    public String toString() {
        return "ServerCodeConfig{" +
                "serverCode='" + serverCode + '\'' +
                ", serverName='" + serverName + '\'' +
                ", updateInterval=" + updateInterval +
                '}';
    }
}