package com.king.sgrlgl.managers;

import com.king.sgrlgl.Main;
import com.king.sgrlgl.game.Arena;
import com.king.sgrlgl.game.GameState;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages game arenas and player data
 * 
 * @author King
 */
public class GameManager {

    private final Main plugin;
    private final ConfigManager configManager;
    private final Map<String, Arena> arenas;

    // Player data storage
    private Location lobby;
    private Location guestLobby;
    private Location finish;

    private final Set<UUID> admins = new HashSet<>();
    private final Set<UUID> guests = new HashSet<>();
    private final Set<UUID> winners = new HashSet<>();

    public GameManager(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.arenas = new HashMap<>();
        loadAll();
    }

    /**
     * Load all data from configuration
     */
    public void loadAll() {
        FileConfiguration config = plugin.getConfig();
        
        lobby = readLocation(config, "locations.lobby");
        guestLobby = readLocation(config, "locations.guestLobby");
        finish = readLocation(config, "locations.finish");

        admins.clear();
        admins.addAll(readUUIDList(config, "roles.admins"));
        
        guests.clear();
        guests.addAll(readUUIDList(config, "roles.guests"));
        
        winners.clear();
        winners.addAll(readUUIDList(config, "roles.winners"));
    }

    /**
     * Save all data to configuration
     */
    public void saveAll() {
        FileConfiguration config = plugin.getConfig();
        
        writeLocation(config, "locations.lobby", lobby);
        writeLocation(config, "locations.guestLobby", guestLobby);
        writeLocation(config, "locations.finish", finish);

        writeUUIDList(config, "roles.admins", admins);
        writeUUIDList(config, "roles.guests", guests);
        writeUUIDList(config, "roles.winners", winners);

        plugin.saveConfig();
    }

    /**
     * Create a new arena
     * @param name Arena name
     * @return Created arena
     */
    public Arena createArena(String name) {
        Arena arena = new Arena(name, this, configManager);
        arenas.put(name.toLowerCase(), arena);
        return arena;
    }

    /**
     * Get an arena by name
     * @param name Arena name
     * @return Arena or null if not found
     */
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    /**
     * Get all arenas
     * @return Collection of arenas
     */
    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    /**
     * Stop all active games
     */
    public void stopAllGames() {
        for (Arena arena : arenas.values()) {
            if (arena.getState() != GameState.WAITING) {
                arena.stopGame();
            }
        }
    }

    /**
     * Apply world rules for the game
     * @param world World to apply rules to
     */
    public void applyWorldRules(World world) {
        if (world == null) return;
        
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
        world.setClearWeatherDuration(20 * 60 * 10);
    }

    /**
     * Check if two locations are in the same block
     * @param a First location
     * @param b Second location
     * @return True if same block
     */
    public static boolean sameBlock(Location a, Location b) {
        if (a == null || b == null || a.getWorld() == null || b.getWorld() == null) return false;
        if (!a.getWorld().equals(b.getWorld())) return false;
        return a.getBlockX() == b.getBlockX()
            && a.getBlockY() == b.getBlockY()
            && a.getBlockZ() == b.getBlockZ();
    }

    // Helper methods for configuration I/O
    private Location readLocation(FileConfiguration config, String path) {
        ConfigurationSection sec = config.getConfigurationSection(path);
        if (sec == null || !sec.isSet("world")) return null;
        
        World w = Bukkit.getWorld(sec.getString("world"));
        if (w == null) return null;
        
        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        float yaw = (float) sec.getDouble("yaw", 0.0);
        float pitch = (float) sec.getDouble("pitch", 0.0);
        
        return new Location(w, x, y, z, yaw, pitch);
    }

    private void writeLocation(FileConfiguration config, String path, Location loc) {
        if (loc == null) {
            config.set(path, null);
            return;
        }
        
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }

    private List<UUID> readUUIDList(FileConfiguration config, String path) {
        return config.getStringList(path).stream()
                .map(s -> {
                    try {
                        return UUID.fromString(s);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void writeUUIDList(FileConfiguration config, String path, Collection<UUID> uuids) {
        List<String> list = uuids.stream().map(UUID::toString).collect(Collectors.toList());
        config.set(path, list);
    }

    // Getters and setters
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; saveAll(); }

    public Location getGuestLobby() { return guestLobby; }
    public void setGuestLobby(Location guestLobby) { this.guestLobby = guestLobby; saveAll(); }

    public Location getFinish() { return finish; }
    public void setFinish(Location finish) { this.finish = finish; saveAll(); }

    public boolean isAdmin(UUID id) { return admins.contains(id); }
    public boolean isGuest(UUID id) { return guests.contains(id); }
    public boolean isWinner(UUID id) { return winners.contains(id); }

    public void addAdmin(UUID id) { admins.add(id); saveAll(); }
    public void addGuest(UUID id) { guests.add(id); saveAll(); }
    public void addWinner(UUID id) { winners.add(id); saveAll(); }

    public void removeAdmin(UUID id) { admins.remove(id); saveAll(); }
    public void removeGuest(UUID id) { guests.remove(id); saveAll(); }
    public void removeWinner(UUID id) { winners.remove(id); saveAll(); }

    public boolean isImmune(UUID id) { return isAdmin(id) || isGuest(id) || isWinner(id); }

    public Main getPlugin() { return plugin; }
    public ConfigManager getConfigManager() { return configManager; }
}