package com.gameboy.connector.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 명령어 실행 결과를 캡처하는 CommandSender
 */
public class CommandResultCapture implements CommandSender {
    private final ConsoleCommandSender consoleSender;
    private final List<String> messages = new ArrayList<>();

    public CommandResultCapture() {
        this.consoleSender = Bukkit.getConsoleSender();
    }

    @Override
    public void sendMessage(String message) {
        messages.add(message);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            this.messages.add(message);
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {
        messages.add(message);
    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
        for (String message : messages) {
            this.messages.add(message);
        }
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public String getMessagesAsString() {
        return String.join("\n", messages);
    }

    // 나머지 필수 메서드들은 콘솔에 위임
    @Override
    public Server getServer() {
        return consoleSender.getServer();
    }

    @Override
    public String getName() {
        return "WebCommandExecutor";
    }

    @Override
    public Spigot spigot() {
        return consoleSender.spigot();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(String name) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return consoleSender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return consoleSender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return consoleSender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return consoleSender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        consoleSender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        consoleSender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return consoleSender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        // no-op
    }
}
