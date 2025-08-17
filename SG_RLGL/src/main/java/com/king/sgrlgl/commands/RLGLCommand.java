package com.king.sgrlgl.commands;

import com.king.sgrlgl.Main;
import com.king.sgrlgl.game.Arena;
import com.king.sgrlgl.game.GameState;
import com.king.sgrlgl.listeners.InteractListener;
import com.king.sgrlgl.managers.ConfigManager;
import com.king.sgrlgl.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main command handler for the RLGL plugin
 * 
 * @author King
 */
public class RLGLCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final GameManager gameManager;
    private final ConfigManager configManager;

    public RLGLCommand(Main plugin, GameManager gameManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                return showHelp(sender);
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender, args);
            case "reload":
                return handleReload(sender);
            case "set":
                return handleSet(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "tp":
                return handleTeleport(sender, args);
            case "arena":
                return handleArena(sender, args);
            default:
                return showHelp(sender);
        }
    }

    /**
     * Show help message
     */
    private boolean showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== SG_RLGL Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl start [arena] " + ChatColor.GRAY + "- Start a game");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl stop [arena] " + ChatColor.GRAY + "- Stop a game");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl set <lobby|guestlobby|finish|admin|guest> [player] " + ChatColor.GRAY + "- Set locations/roles");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl remove <admin|guest|winner> <player> " + ChatColor.GRAY + "- Remove roles");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl tp <guest|admin|player> <guestlobby|gamelobby> " + ChatColor.GRAY + "- Teleport players");
        sender.sendMessage(ChatColor.YELLOW + "/rlgl arena <create|delete|list> [name] " + ChatColor.GRAY + "- Manage arenas");
        return true;
    }

    /**
     * Handle start command
     */
    private boolean handleStart(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) return true;
        
        String arenaName = args.length > 1 ? args[1] : "default";
        Arena arena = gameManager.getArena(arenaName);
        
        if (arena == null) {
            arena = gameManager.createArena(arenaName);
        }
        
        if (arena.getState() != GameState.WAITING) {
            sender.sendMessage(configManager.getMessage("game-already-active", "arena", arenaName));
            return true;
        }
        
        arena.startGame();
        sender.sendMessage(configManager.getMessage("game-started", "arena", arenaName));
        
        // Give admin dyes to admins
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameManager.isAdmin(player.getUniqueId())) {
                InteractListener.giveAdminDyes(player);
            }
        }
        
        return true;
    }

    /**
     * Handle stop command
     */
    private boolean handleStop(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) return true;
        
        String arenaName = args.length > 1 ? args[1] : "default";
        Arena arena = gameManager.getArena(arenaName);
        
        if (arena == null || arena.getState() == GameState.WAITING) {
            sender.sendMessage(configManager.getMessage("no-active-game", "arena", arenaName));
            return true;
        }
        
        arena.stopGame();
        sender.sendMessage(configManager.getMessage("game-stopped", "arena", arenaName));
        
        // Remove admin dyes
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameManager.isAdmin(player.getUniqueId())) {
                InteractListener.removeAdminDyes(player);
            }
        }
        
        return true;
    }

    /**
     * Handle reload command
     */
    private boolean handleReload(CommandSender sender) {
        if (!hasPermission(sender)) return true;
        
        configManager.loadConfig();
        gameManager.loadAll();
        sender.sendMessage(configManager.getMessage("config-reloaded"));
        return true;
    }

    /**
     * Handle set command
     */
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) return true;
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rlgl set <lobby|guestlobby|finish|admin|guest|rules> [player]");
            return true;
        }

        String type = args[1].toLowerCase();
        
        switch (type) {
            case "lobby":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can set locations.");
                    return true;
                }
                gameManager.setLobby(((Player) sender).getLocation());
                sender.sendMessage(configManager.getMessage("lobby-set"));
                return true;
                
            case "guestlobby":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can set locations.");
                    return true;
                }
                gameManager.setGuestLobby(((Player) sender).getLocation());
                sender.sendMessage(configManager.getMessage("guest-lobby-set"));
                return true;
                
            case "finish":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can set locations.");
                    return true;
                }
                InteractListener.giveFinishHoe((Player) sender);
                sender.sendMessage(configManager.getMessage("finish-hoe-given"));
                return true;
                
            case "admin":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rlgl set admin <player>");
                    return true;
                }
                Player adminPlayer = Bukkit.getPlayerExact(args[2]);
                if (adminPlayer == null) {
                    sender.sendMessage(configManager.getMessage("player-not-found", "player", args[2]));
                    return true;
                }
                gameManager.addAdmin(adminPlayer.getUniqueId());
                sender.sendMessage(configManager.getMessage("admin-added", "player", adminPlayer.getName()));
                return true;
                
            case "guest":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rlgl set guest <player>");
                    return true;
                }
                Player guestPlayer = Bukkit.getPlayerExact(args[2]);
                if (guestPlayer == null) {
                    sender.sendMessage(configManager.getMessage("player-not-found", "player", args[2]));
                    return true;
                }
                gameManager.addGuest(guestPlayer.getUniqueId());
                sender.sendMessage(configManager.getMessage("guest-added", "player", guestPlayer.getName()));
                return true;
                
            case "rules":
                World world = sender instanceof Player ? ((Player) sender).getWorld() : Bukkit.getWorlds().get(0);
                gameManager.applyWorldRules(world);
                sender.sendMessage(configManager.getMessage("rules-applied"));
                return true;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown set option: " + type);
                return true;
        }
    }

    /**
     * Handle remove command
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) return true;
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /rlgl remove <admin|guest|winner> <player>");
            return true;
        }

        String role = args[1].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        UUID targetId = target.getUniqueId();

        switch (role) {
            case "admin":
                gameManager.removeAdmin(targetId);
                sender.sendMessage(configManager.getMessage("admin-removed", "player", target.getName()));
                return true;
            case "guest":
                gameManager.removeGuest(targetId);
                sender.sendMessage(configManager.getMessage("guest-removed", "player", target.getName()));
                return true;
            case "winner":
                gameManager.removeWinner(targetId);
                sender.sendMessage(configManager.getMessage("winner-removed", "player", target.getName()));
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown role: " + role);
                return true;
        }
    }

    /**
     * Handle teleport command
     */
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) return true;
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /rlgl tp <guest|admin|player> <guestlobby|gamelobby>");
            return true;
        }

        String who = args[1].toLowerCase();
        String where = args[2].toLowerCase();

        org.bukkit.Location targetLocation = null;
        if (where.equals("guestlobby")) {
            targetLocation = gameManager.getGuestLobby();
        } else if (where.equals("gamelobby")) {
            targetLocation = gameManager.getLobby();
        }

        if (targetLocation == null) {
            sender.sendMessage(configManager.getMessage("location-not-set"));
            return true;
        }

        int count = 0;
        switch (who) {
            case "guest":
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (gameManager.isGuest(player.getUniqueId())) {
                        player.teleport(targetLocation);
                        count++;
                    }
                }
                sender.sendMessage(configManager.getMessage("guests-teleported", "count", String.valueOf(count)));
                return true;
            case "admin":
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (gameManager.isAdmin(player.getUniqueId())) {
                        player.teleport(targetLocation);
                        count++;
                    }
                }
                sender.sendMessage(configManager.getMessage("admins-teleported", "count", String.valueOf(count)));
                return true;
            case "player":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can teleport themselves.");
                    return true;
                }
                ((Player) sender).teleport(targetLocation);
                sender.sendMessage(configManager.getMessage("teleported"));
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown target: " + who);
                return true;
        }
    }

    /**
     * Handle arena command
     */
    private boolean handleArena(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) return true;
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rlgl arena <create|delete|list> [name]");
            return true;
        }

        String action = args[1].toLowerCase();
        
        switch (action) {
            case "create":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rlgl arena create <name>");
                    return true;
                }
                String createName = args[2];
                if (gameManager.getArena(createName) != null) {
                    sender.sendMessage(configManager.getMessage("arena-already-exists", "arena", createName));
                    return true;
                }
                gameManager.createArena(createName);
                sender.sendMessage(configManager.getMessage("arena-created", "arena", createName));
                return true;
                
            case "list":
                Collection<Arena> arenas = gameManager.getArenas();
                if (arenas.isEmpty()) {
                    sender.sendMessage(configManager.getMessage("no-arenas"));
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Arenas:");
                for (Arena arena : arenas) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + arena.getName() + 
                            ChatColor.GRAY + " (" + arena.getState() + ")");
                }
                return true;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown arena action: " + action);
                return true;
        }
    }

    /**
     * Check if sender has permission
     */
    private boolean hasPermission(CommandSender sender) {
        if (sender.isOp()) return true;
        if (sender instanceof Player) {
            return gameManager.isAdmin(((Player) sender).getUniqueId());
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "start", "stop", "reload", "set", "remove", "tp", "arena")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "set":
                    return Arrays.asList("lobby", "guestlobby", "finish", "admin", "guest", "rules");
                case "remove":
                    return Arrays.asList("admin", "guest", "winner");
                case "tp":
                    return Arrays.asList("guest", "admin", "player");
                case "arena":
                    return Arrays.asList("create", "delete", "list");
                case "start":
                case "stop":
                    return gameManager.getArenas().stream()
                            .map(Arena::getName)
                            .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "tp":
                    return Arrays.asList("guestlobby", "gamelobby");
                case "set":
                    if (args[1].equalsIgnoreCase("admin") || args[1].equalsIgnoreCase("guest")) {
                        return null; // Show online players
                    }
                    break;
                case "remove":
                    return null; // Show all players
            }
        }
        
        return Collections.emptyList();
    }
}