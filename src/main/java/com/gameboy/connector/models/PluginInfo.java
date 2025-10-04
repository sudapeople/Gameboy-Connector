package com.gameboy.connector.models;

/**
 * 플러그인 정보 모델 클래스
 * 서버에 설치된 플러그인 정보를 담습니다.
 */
public class PluginInfo {
    
    private String name;
    private String version;
    private boolean enabled;
    
    public PluginInfo() {
        // 기본 생성자
    }
    
    public PluginInfo(String name, String version, boolean enabled) {
        this.name = name;
        this.version = version;
        this.enabled = enabled;
    }
    
    // Getter 메서드들
    public String getName() {
        return name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    // Setter 메서드들
    public void setName(String name) {
        this.name = name;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public String toString() {
        return "PluginInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}