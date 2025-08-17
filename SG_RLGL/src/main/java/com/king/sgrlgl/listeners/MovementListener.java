package com.king.sgrlgl.listeners;

import com.king.sgrlgl.game.Arena;
import com.king.sgrlgl.game.GameState;
import com.king.sgrlgl.game.LightState;
import com.king.sgrlgl.managers.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * Handles player movement events for the game
 * 
 * @author King
 */
public class MovementListener implements Listener {

    private final GameManager gameManager;

    public MovementListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check finish line detection
        handleFinishDetection(player, event.getTo());

        // Check red light movement
        handleRedLightMovement(player, event);
    }

    /**
     * Handle finish line detection
     */
    private void handleFinishDetection(Player player, Location to) {
        Location finish = gameManager.getFinish();
        if (finish == null || gameManager.isWinner(player.getUniqueId())) return;

        if (GameManager.sameBlock(to, finish)) {
            // Find the arena this player is in
            for (Arena arena : gameManager.getArenas()) {
                if (arena.isPlayerInArena(player.getUniqueId())) {
                    arena.playerReachedFinish(player);
                    break;
                }
            }
        }
    }

    /**
     * Handle red light movement enforcement
     */
    private void handleRedLightMovement(Player player, PlayerMoveEvent event) {
        UUID playerId = player.getUniqueId();
        
        // Check if player is immune
        if (gameManager.isImmune(playerId)) return;

        // Find active arena with red light
        Arena playerArena = null;
        for (Arena arena : gameManager.getArenas()) {
            if (arena.isPlayerInArena(playerId) && 
                arena.getState() == GameState.ACTIVE && 
                arena.getLightState() == LightState.RED) {
                playerArena = arena;
                break;
            }
        }

        if (playerArena == null) return;

        // Check if player moved
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        boolean moved = (from.getX() != to.getX()) || 
                       (from.getY() != to.getY()) || 
                       (from.getZ() != to.getZ()) ||
                       (from.getPitch() != to.getPitch()) || 
                       (from.getYaw() != to.getYaw());

        if (moved) {
            // Cancel the movement
            event.setCancelled(true);
            
            // Eliminate the player
            playerArena.eliminatePlayer(player);
        }
    }
}