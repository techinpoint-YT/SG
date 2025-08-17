package com.king.sgrlgl;

import com.king.sgrlgl.commands.RLGLCommand;
import com.king.sgrlgl.listeners.InteractListener;
import com.king.sgrlgl.listeners.MovementListener;
import com.king.sgrlgl.managers.ConfigManager;
import com.king.sgrlgl.managers.GameManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for SG_RLGL (Squid Game Red Light Green Light)
 * 
 * @author King
 * @version 1.0.0
 */
public final class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.gameManager = new GameManager(this, configManager);

        // Register commands
        RLGLCommand rlglCommand = new RLGLCommand(this, gameManager, configManager);
        getCommand("rlgl").setExecutor(rlglCommand);
        getCommand("rlgl").setTabCompleter(rlglCommand);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MovementListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new InteractListener(gameManager), this);

        getLogger().info("SG_RLGL has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopAllGames();
            gameManager.saveAll();
        }
        getLogger().info("SG_RLGL has been disabled.");
    }

    /**
     * Get the plugin instance
     * @return Main plugin instance
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Get the config manager
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the game manager
     * @return GameManager instance
     */
    public GameManager getGameManager() {
        return gameManager;
    }
}