package com.king.sgrlgl.managers;

import com.king.sgrlgl.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Manages plugin configuration loading, saving, and reloading
 * 
 * @author King
 */
public class ConfigManager {

    private final Main plugin;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Load or reload the configuration
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Save the configuration
     */
    public void saveConfig() {
        plugin.saveConfig();
    }

    /**
     * Get a message with color codes translated and placeholders replaced
     * @param path Configuration path
     * @param placeholders Placeholder replacements (key, value pairs)
     * @return Formatted message
     */
    public String getMessage(String path, String... placeholders) {
        String message = config.getString("messages." + path, "");
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // Replace placeholders
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        
        return message;
    }

    /**
     * Get the prefix message
     * @return Formatted prefix
     */
    public String getPrefix() {
        return getMessage("prefix");
    }

    /**
     * Get minimum red light duration in seconds
     * @return Minimum duration
     */
    public int getMinRedLightDuration() {
        return config.getInt("timers.red-light.min", 3);
    }

    /**
     * Get maximum red light duration in seconds
     * @return Maximum duration
     */
    public int getMaxRedLightDuration() {
        return config.getInt("timers.red-light.max", 8);
    }

    /**
     * Get minimum green light duration in seconds
     * @return Minimum duration
     */
    public int getMinGreenLightDuration() {
        return config.getInt("timers.green-light.min", 5);
    }

    /**
     * Get maximum green light duration in seconds
     * @return Maximum duration
     */
    public int getMaxGreenLightDuration() {
        return config.getInt("timers.green-light.max", 15);
    }

    /**
     * Get elimination method
     * @return Elimination method (KILL, KICK, TELEPORT)
     */
    public String getEliminationMethod() {
        return config.getString("elimination.method", "KICK").toUpperCase();
    }

    /**
     * Check if spectator mode is enabled
     * @return True if spectator mode is enabled
     */
    public boolean isSpectatorModeEnabled() {
        return config.getBoolean("elimination.spectator-mode", true);
    }

    /**
     * Get reward commands for winners
     * @return List of commands to execute
     */
    public List<String> getRewardCommands() {
        return config.getStringList("rewards.commands");
    }

    /**
     * Check if sounds are enabled
     * @return True if sounds are enabled
     */
    public boolean areSoundsEnabled() {
        return config.getBoolean("effects.sounds.enabled", true);
    }

    /**
     * Get green light sound
     * @return Sound name
     */
    public String getGreenLightSound() {
        return config.getString("effects.sounds.green-light", "BLOCK_NOTE_BLOCK_PLING");
    }

    /**
     * Get red light sound
     * @return Sound name
     */
    public String getRedLightSound() {
        return config.getString("effects.sounds.red-light", "BLOCK_NOTE_BLOCK_BASS");
    }

    /**
     * Check if boss bar is enabled
     * @return True if boss bar is enabled
     */
    public boolean isBossBarEnabled() {
        return config.getBoolean("effects.boss-bar.enabled", true);
    }

    /**
     * Check if action bar is enabled
     * @return True if action bar is enabled
     */
    public boolean isActionBarEnabled() {
        return config.getBoolean("effects.action-bar.enabled", true);
    }

    /**
     * Get the underlying FileConfiguration
     * @return FileConfiguration instance
     */
    public FileConfiguration getConfig() {
        return config;
    }
}