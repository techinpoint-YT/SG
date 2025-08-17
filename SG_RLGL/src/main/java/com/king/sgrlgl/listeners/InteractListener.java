package com.king.sgrlgl.listeners;

import com.king.sgrlgl.Main;
import com.king.sgrlgl.game.Arena;
import com.king.sgrlgl.game.GameState;
import com.king.sgrlgl.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles player interaction events for special items
 * 
 * @author King
 */
public class InteractListener implements Listener {

    private final GameManager gameManager;
    
    private static final String HOE_NAME = ChatColor.GOLD + "SG Finish Setter";
    private static NamespacedKey HOE_KEY;
    private static NamespacedKey DYE_KEY;

    public InteractListener(GameManager gameManager) {
        this.gameManager = gameManager;
        HOE_KEY = new NamespacedKey(Main.getInstance(), "sg_finish_hoe");
        DYE_KEY = new NamespacedKey(Main.getInstance(), "sg_admin_dye");
    }

    /**
     * Give finish setting hoe to player
     * @param player Player to give hoe to
     */
    public static void giveFinishHoe(Player player) {
        ItemStack item = new ItemStack(Material.NETHERITE_HOE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(HOE_NAME);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(HOE_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }

    /**
     * Give admin control dyes to player
     * @param player Player to give dyes to
     */
    public static void giveAdminDyes(Player player) {
        ItemStack green = new ItemStack(Material.GREEN_DYE, 1);
        ItemMeta greenMeta = green.getItemMeta();
        greenMeta.setDisplayName(ChatColor.GREEN + "Green Light");
        greenMeta.getPersistentDataContainer().set(DYE_KEY, PersistentDataType.STRING, "GREEN");
        green.setItemMeta(greenMeta);

        ItemStack red = new ItemStack(Material.RED_DYE, 1);
        ItemMeta redMeta = red.getItemMeta();
        redMeta.setDisplayName(ChatColor.RED + "Red Light");
        redMeta.getPersistentDataContainer().set(DYE_KEY, PersistentDataType.STRING, "RED");
        red.setItemMeta(redMeta);

        player.getInventory().addItem(green, red);
    }

    /**
     * Remove admin control dyes from player
     * @param player Player to remove dyes from
     */
    public static void removeAdminDyes(Player player) {
        player.getInventory().remove(Material.GREEN_DYE);
        player.getInventory().remove(Material.RED_DYE);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !item.hasItemMeta()) return;

        // Handle finish hoe
        if (handleFinishHoe(event, player, item)) return;
        
        // Handle admin dyes
        handleAdminDyes(event, player, item);
    }

    /**
     * Handle finish hoe interaction
     */
    private boolean handleFinishHoe(PlayerInteractEvent event, Player player, ItemStack item) {
        if (item.getType() != Material.NETHERITE_HOE) return false;
        if (!item.getItemMeta().getPersistentDataContainer().has(HOE_KEY, PersistentDataType.BYTE)) return false;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            
            org.bukkit.Location blockLocation = event.getClickedBlock().getLocation().add(0.5, 0, 0.5);
            blockLocation.setYaw(0f);
            blockLocation.setPitch(0f);
            
            gameManager.setFinish(blockLocation);
            
            String message = gameManager.getConfigManager().getMessage("finish-set",
                    "x", String.valueOf(blockLocation.getBlockX()),
                    "y", String.valueOf(blockLocation.getBlockY()),
                    "z", String.valueOf(blockLocation.getBlockZ()),
                    "world", blockLocation.getWorld().getName());
            player.sendMessage(message);
        }
        return true;
    }

    /**
     * Handle admin dye interaction
     */
    private void handleAdminDyes(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!item.getItemMeta().getPersistentDataContainer().has(DYE_KEY, PersistentDataType.STRING)) return;

        String dyeType = item.getItemMeta().getPersistentDataContainer().get(DYE_KEY, PersistentDataType.STRING);
        if (dyeType == null) return;

        event.setCancelled(true);

        // Check if player is admin
        if (!gameManager.isAdmin(player.getUniqueId())) {
            player.sendMessage(gameManager.getConfigManager().getMessage("no-permission"));
            return;
        }

        // Find active arena (for now, use default arena)
        Arena arena = gameManager.getArena("default");
        if (arena == null || arena.getState() != GameState.ACTIVE) {
            player.sendMessage(gameManager.getConfigManager().getMessage("no-active-game", "arena", "default"));
            return;
        }

        // This would need to be implemented in Arena class to manually control lights
        // For now, we'll just send a message
        String message = gameManager.getConfigManager().getMessage("manual-light-control-disabled");
        player.sendMessage(message);
    }
}