package fr.mathilde.moreVanillaAdvancements.gui;

import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class AdminSettingsGUI implements Listener {
    private final JavaPlugin plugin;
    private final AchievementService service;

    private static final String TITLE = ChatColor.DARK_AQUA + "MVA Settings";

    public AdminSettingsGUI(JavaPlugin plugin, AchievementService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(p, 9, TITLE);
        FileConfiguration cfg = plugin.getConfig();
        boolean bcast = cfg.getBoolean("settings.broadcastChat", true);
        boolean title = cfg.getBoolean("settings.showTitle", true);
        inv.setItem(3, toggleItem(Material.PAPER, ChatColor.GOLD + "Broadcast Chat: " + colorBool(bcast), bcast));
        inv.setItem(5, toggleItem(Material.NAME_TAG, ChatColor.GOLD + "Title: " + colorBool(title), title));
        p.openInventory(inv);
    }

    private ItemStack toggleItem(Material mat, String name, boolean on) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Clique pour basculer", on ? ChatColor.GREEN + "Activé" : ChatColor.RED + "Désactivé"));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);
        }
        return it;
    }

    private String colorBool(boolean b) { return (b ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"); }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        HumanEntity clicker = e.getWhoClicked();
        if (!(clicker instanceof Player)) return;
        Player p = (Player) clicker;
        int slot = e.getRawSlot();
        FileConfiguration cfg = plugin.getConfig();
        if (slot == 3) {
            boolean cur = cfg.getBoolean("settings.broadcastChat", true);
            cfg.set("settings.broadcastChat", !cur);
            plugin.saveConfig();
            service.reloadSettings(cfg);
            open(p);
        } else if (slot == 5) {
            boolean cur = cfg.getBoolean("settings.showTitle", true);
            cfg.set("settings.showTitle", !cur);
            plugin.saveConfig();
            service.reloadSettings(cfg);
            open(p);
        }
    }
}

