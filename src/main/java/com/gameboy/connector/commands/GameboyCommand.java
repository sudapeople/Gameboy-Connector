package com.gameboy.connector.commands;

import com.gameboy.connector.GameboyConnector;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * GameBoy Connector 명령어 처리 클래스
 */
public class GameboyCommand {
    
    private final GameboyConnector plugin;
    
    public GameboyCommand(GameboyConnector plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gameboy.admin")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "test":
                handleTest(sender);
                break;
            case "help":
                showHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다. /gameboy help를 사용하세요.");
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== GameBoy Connector 도움말 ===");
        sender.sendMessage(ChatColor.YELLOW + "/gameboy reload" + ChatColor.WHITE + " - 플러그인 설정 다시 로드");
        sender.sendMessage(ChatColor.YELLOW + "/gameboy info" + ChatColor.WHITE + " - 서버 정보 표시");
        sender.sendMessage(ChatColor.YELLOW + "/gameboy test" + ChatColor.WHITE + " - 웹서버 연결 테스트");
        sender.sendMessage(ChatColor.YELLOW + "/gameboy help" + ChatColor.WHITE + " - 이 도움말 표시");
    }
    
    private void handleReload(CommandSender sender) {
        try {
            // config.yml 다시 로드
            plugin.reloadConfig();
            
            // 플러그인 설정 다시 로드
            plugin.loadPluginConfig();
            
            sender.sendMessage(ChatColor.GREEN + "플러그인 설정이 다시 로드되었습니다.");
            sender.sendMessage(ChatColor.YELLOW + "변경된 설정이 적용되었습니다.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "설정 다시 로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private void handleInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== GameBoy Connector 정보 ===");
        sender.sendMessage(ChatColor.YELLOW + "플러그인 버전: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "서버 코드: " + ChatColor.WHITE + plugin.getServerConfig().getServerCode());
        sender.sendMessage(ChatColor.YELLOW + "서버 이름: " + ChatColor.WHITE + plugin.getServerConfig().getServerName());
        sender.sendMessage(ChatColor.YELLOW + "업데이트 간격: " + ChatColor.WHITE + plugin.getServerConfig().getUpdateInterval() + "초");
        sender.sendMessage(ChatColor.YELLOW + "플러그인 상태: " + ChatColor.WHITE + (plugin.isPluginEnabled() ? "활성화" : "비활성화"));
        sender.sendMessage(ChatColor.YELLOW + "Java 버전: " + ChatColor.WHITE + System.getProperty("java.version"));
        sender.sendMessage(ChatColor.YELLOW + "서버 버전: " + ChatColor.WHITE + plugin.getServer().getVersion());
    }
    
    private void handleTest(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "웹서버 연결 테스트 중...");

        // 비동기로 테스트 실행
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // testConnection() 메서드 호출
                boolean success = plugin.getCommunicator().testConnection();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (success) {
                        sender.sendMessage(ChatColor.GREEN + "웹서버 연결 테스트가 완료되었습니다.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "웹서버 연결 테스트 실패");
                    }
                });
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(ChatColor.RED + "웹서버 연결 테스트 실패: " + e.getMessage());
                });
            }
        });
    }
}