package com.gameboy.connector.listeners;

import com.gameboy.connector.GameboyConnector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 플레이어 이벤트 리스너
 * 플레이어 접속/퇴장 이벤트를 처리합니다.
 */
public class PlayerEventListener implements Listener {
    
    private final GameboyConnector plugin;
    
    public PlayerEventListener(GameboyConnector plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 플레이어 접속 시 로그 기록
        plugin.getPluginLogger().info("플레이어 접속: " + event.getPlayer().getName());
        
        // 웹서버에 플레이어 접속 알림 (선택사항)
        // plugin.getCommunicator().sendPlayerJoinNotification(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 플레이어 퇴장 시 로그 기록
        plugin.getPluginLogger().info("플레이어 퇴장: " + event.getPlayer().getName());
        
        // 웹서버에 플레이어 퇴장 알림 (선택사항)
        // plugin.getCommunicator().sendPlayerQuitNotification(event.getPlayer());
    }
}