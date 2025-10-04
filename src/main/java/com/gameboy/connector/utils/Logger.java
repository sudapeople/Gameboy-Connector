package com.gameboy.connector.utils;

import com.gameboy.connector.GameboyConnector;
import org.bukkit.ChatColor;

/**
 * 플러그인 전용 로거 클래스
 * 플러그인의 로깅을 관리합니다.
 */
public class Logger {
    
    private final GameboyConnector plugin;
    private final String prefix;
    
    public Logger(GameboyConnector plugin) {
        this.plugin = plugin;
        this.prefix = ChatColor.GOLD + "[GameBoyConnector] " + ChatColor.RESET;
    }
    
    /**
     * 정보 로그 출력
     */
    public void info(String message) {
        plugin.getLogger().info(prefix + message);
    }
    
    /**
     * 경고 로그 출력
     */
    public void warning(String message) {
        plugin.getLogger().warning(prefix + message);
    }
    
    /**
     * 심각한 오류 로그 출력
     */
    public void severe(String message) {
        plugin.getLogger().severe(prefix + message);
    }
    
    /**
     * 디버그 로그 출력 (설정에 따라)
     */
    public void debug(String message) {
        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info(prefix + "[DEBUG] " + message);
        }
    }
    
    /**
     * 예외 로그 출력
     */
    public void exception(String message, Throwable throwable) {
        severe(message + ": " + throwable.getMessage());
        if (plugin.getConfig().getBoolean("logging.log_errors", true)) {
            throwable.printStackTrace();
        }
    }
}