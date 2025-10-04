package com.gameboy.connector.models;

/**
 * 플레이어 정보 모델 클래스
 * 웹서버로 전송할 플레이어 정보를 담습니다.
 */
public class PlayerInfo {
    
    private String playerId;
    private String playerUuid;
    
    public PlayerInfo() {
        // 기본 생성자
    }
    
    public PlayerInfo(String playerId, String playerUuid) {
        this.playerId = playerId;
        this.playerUuid = playerUuid;
    }
    
    // Getter 메서드들
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerUuid() {
        return playerUuid;
    }
    
    // Setter 메서드들
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }
    
    @Override
    public String toString() {
        return "PlayerInfo{" +
                "playerId='" + playerId + '\'' +
                ", playerUuid='" + playerUuid + '\'' +
                '}';
    }
}