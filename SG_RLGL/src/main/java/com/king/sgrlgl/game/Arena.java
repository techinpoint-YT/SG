package com.king.sgrlgl.game;

import com.king.sgrlgl.managers.ConfigManager;
import com.king.sgrlgl.managers.GameManager;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Represents a game arena for Red Light Green Light
 * 
 * @author King
 */
public class Arena {

    private final String name;
    private final GameManager gameManager;
    private final ConfigManager configManager;
    
    private GameState state;
    private LightState lightState;
    private final Set<UUID> players;
    private final Set<UUID> spectators;
    private BossBar bossBar;
    private BukkitTask gameTask;
    private int timeRemaining;

    public Arena(String name, GameManager gameManager, ConfigManager configManager) {
        this.name = name;
        this.gameManager = gameManager;
        this.configManager = configManager;
        this.state = GameState.WAITING;
        this.lightState = LightState.GREEN;
        this.players = new HashSet<>();
        this.spectators = new HashSet<>();
        
        if (configManager.isBossBarEnabled()) {
            this.bossBar = Bukkit.createBossBar("Red Light Green Light", BarColor.GREEN, BarStyle.SOLID);
        }
    }

    /**
     * Start the game
     */
    public void startGame() {
        if (state != GameState.WAITING) return;
        
        state = GameState.ACTIVE;
        lightState = LightState.GREEN;
        
        // Broadcast start message
        broadcastToPlayers(configManager.getMessage("game-start", "arena", name));
        
        // Start game loop
        startGameLoop();
        
        // Play sound and update displays
        updateEffects();
    }

    /**
     * Stop the game
     */
    public void stopGame() {
        if (state == GameState.WAITING) return;
        
        state = GameState.WAITING;
        
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        
        if (bossBar != null) {
            bossBar.removeAll();
        }
        
        // Clear spectators and reset players
        spectators.clear();
        
        broadcastToPlayers(configManager.getMessage("game-stopped", "arena", name));
    }

    /**
     * Start the game loop with random timers
     */
    private void startGameLoop() {
        if (gameTask != null) {
            gameTask.cancel();
        }
        
        // Determine next phase duration
        Random random = new Random();
        if (lightState == LightState.GREEN) {
            int min = configManager.getMinGreenLightDuration();
            int max = configManager.getMaxGreenLightDuration();
            timeRemaining = min + random.nextInt(max - min + 1);
        } else {
            int min = configManager.getMinRedLightDuration();
            int max = configManager.getMaxRedLightDuration();
            timeRemaining = min + random.nextInt(max - min + 1);
        }
        
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                timeRemaining--;
                updateBossBar();
                
                if (timeRemaining <= 0) {
                    // Switch phase
                    lightState = (lightState == LightState.GREEN) ? LightState.RED : LightState.GREEN;
                    updateEffects();
                    startGameLoop(); // Start next phase
                }
            }
        }.runTaskTimer(gameManager.getPlugin(), 0L, 20L); // Run every second
    }

    /**
     * Update visual and audio effects
     */
    private void updateEffects() {
        String message;
        String actionBarMessage;
        BarColor barColor;
        String soundName;
        
        if (lightState == LightState.GREEN) {
            message = configManager.getMessage("green-light", "arena", name);
            actionBarMessage = configManager.getMessage("green-light-action", "arena", name);
            barColor = BarColor.GREEN;
            soundName = configManager.getGreenLightSound();
        } else {
            message = configManager.getMessage("red-light", "arena", name);
            actionBarMessage = configManager.getMessage("red-light-action", "arena", name);
            barColor = BarColor.RED;
            soundName = configManager.getRedLightSound();
        }
        
        // Update boss bar
        if (bossBar != null) {
            bossBar.setTitle(message);
            bossBar.setColor(barColor);
            updateBossBar();
        }
        
        // Send messages and effects to players
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Title
                player.sendTitle(message, "", 5, 40, 5);
                
                // Action bar
                if (configManager.isActionBarEnabled()) {
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarMessage));
                }
                
                // Sound
                if (configManager.areSoundsEnabled()) {
                    try {
                        Sound sound = Sound.valueOf(soundName);
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (IllegalArgumentException e) {
                        // Invalid sound name, ignore
                    }
                }
                
                // Add to boss bar
                if (bossBar != null) {
                    bossBar.addPlayer(player);
                }
            }
        }
        
        // Broadcast to chat
        broadcastToPlayers(message);
    }

    /**
     * Update boss bar progress
     */
    private void updateBossBar() {
        if (bossBar == null) return;
        
        int totalTime;
        if (lightState == LightState.GREEN) {
            int min = configManager.getMinGreenLightDuration();
            int max = configManager.getMaxGreenLightDuration();
            totalTime = (min + max) / 2; // Use average for progress calculation
        } else {
            int min = configManager.getMinRedLightDuration();
            int max = configManager.getMaxRedLightDuration();
            totalTime = (min + max) / 2;
        }
        
        double progress = Math.max(0.0, Math.min(1.0, (double) timeRemaining / totalTime));
        bossBar.setProgress(progress);
    }

    /**
     * Eliminate a player
     * @param player Player to eliminate
     */
    public void eliminatePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        players.remove(playerId);
        
        String method = configManager.getEliminationMethod();
        String eliminationMessage = configManager.getMessage("eliminated", 
                "player", player.getName(), "arena", name);
        
        switch (method) {
            case "KILL":
                player.setHealth(0);
                break;
            case "KICK":
                player.kickPlayer(eliminationMessage);
                return; // Don't add to spectators if kicked
            case "TELEPORT":
                Location lobby = gameManager.getLobby();
                if (lobby != null) {
                    player.teleport(lobby);
                }
                break;
        }
        
        // Add to spectators if spectator mode is enabled
        if (configManager.isSpectatorModeEnabled() && !method.equals("KICK")) {
            spectators.add(playerId);
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(configManager.getMessage("spectator-mode", "arena", name));
        }
        
        // Broadcast elimination
        broadcastToPlayers(eliminationMessage);
        
        // Strike lightning for effect
        player.getWorld().strikeLightning(player.getLocation());
    }

    /**
     * Handle player reaching finish
     * @param player Player who reached finish
     */
    public void playerReachedFinish(Player player) {
        UUID playerId = player.getUniqueId();
        if (!players.contains(playerId)) return;
        
        gameManager.addWinner(playerId);
        players.remove(playerId);
        
        // Send win message
        String winMessage = configManager.getMessage("player-won", 
                "player", player.getName(), "arena", name);
        player.sendMessage(winMessage);
        broadcastToPlayers(winMessage);
        
        // Execute reward commands
        List<String> rewardCommands = configManager.getRewardCommands();
        for (String command : rewardCommands) {
            command = command.replace("{player}", player.getName())
                           .replace("{arena}", name);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        
        // Check if game should end
        if (players.isEmpty()) {
            stopGame();
        }
    }

    /**
     * Add a player to the arena
     * @param player Player to add
     */
    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    /**
     * Remove a player from the arena
     * @param player Player to remove
     */
    public void removePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        players.remove(playerId);
        spectators.remove(playerId);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    /**
     * Broadcast a message to all players in the arena
     * @param message Message to broadcast
     */
    private void broadcastToPlayers(String message) {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
        
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                spectator.sendMessage(message);
            }
        }
    }

    // Getters
    public String getName() { return name; }
    public GameState getState() { return state; }
    public LightState getLightState() { return lightState; }
    public Set<UUID> getPlayers() { return new HashSet<>(players); }
    public Set<UUID> getSpectators() { return new HashSet<>(spectators); }
    public boolean isPlayerInArena(UUID playerId) { return players.contains(playerId); }
    public boolean isSpectator(UUID playerId) { return spectators.contains(playerId); }
}