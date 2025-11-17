package fr.mathilde.moreVanillaAdvancements.gui;

import fr.mathilde.moreVanillaAdvancements.lang.LangManager;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import org.bukkit.Bukkit;
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

import java.util.List;

public class AdminSettingsGUI implements Listener {
    private final JavaPlugin plugin;
    private final AchievementService service;
    private final LangManager langManager;

    public AdminSettingsGUI(JavaPlugin plugin, AchievementService service, LangManager langManager) {
        this.plugin = plugin;
        this.service = service;
        this.langManager = langManager;
    }

    public void open(Player p) {
        String title = langManager.getMessage("admin.settings.title");
        Inventory inv = Bukkit.createInventory(p, 9, title);
        FileConfiguration cfg = plugin.getConfig();
        boolean bcast = cfg.getBoolean("settings.broadcastChat", true);
        boolean titleEnabled = cfg.getBoolean("settings.showTitle", true);

        // Broadcast item
        ItemStack broadcastItem = new ItemStack(Material.PAPER);
        ItemMeta broadcastMeta = broadcastItem.getItemMeta();
        if (broadcastMeta != null) {
            broadcastMeta.setDisplayName(langManager.getMessage("admin.settings.broadcast.name"));
            List<String> lore = bcast ?
                langManager.getMessageList("admin.settings.broadcast.lore-enabled") :
                langManager.getMessageList("admin.settings.broadcast.lore-disabled");
            broadcastMeta.setLore(lore);
            broadcastMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            broadcastItem.setItemMeta(broadcastMeta);
        }

        // Title item
        ItemStack titleItem = new ItemStack(Material.NAME_TAG);
        ItemMeta titleMeta = titleItem.getItemMeta();
        if (titleMeta != null) {
            titleMeta.setDisplayName(langManager.getMessage("admin.settings.private-title.name"));
            List<String> lore = titleEnabled ?
                langManager.getMessageList("admin.settings.private-title.lore-enabled") :
                langManager.getMessageList("admin.settings.private-title.lore-disabled");
            titleMeta.setLore(lore);
            titleMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            titleItem.setItemMeta(titleMeta);
        }

        // Close item
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(langManager.getMessage("admin.settings.close.name"));
            closeItem.setItemMeta(closeMeta);
        }

        inv.setItem(3, broadcastItem);
        inv.setItem(5, titleItem);
        inv.setItem(8, closeItem);
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = langManager.getMessage("admin.settings.title");
        if (!e.getView().getTitle().equals(title)) return;

        // Annuler TOUS les clics (y compris dans l'inventaire du joueur)
        e.setCancelled(true);

        HumanEntity clicker = e.getWhoClicked();
        if (!(clicker instanceof Player)) return;
        Player p = (Player) clicker;

        // Si le joueur clique dans son propre inventaire, ne rien faire (juste bloquer le clic)
        if (e.getClickedInventory() == p.getInventory()) {
            return;
        }

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
        } else if (slot == 8) {
            // Bouton fermer
            p.closeInventory();
        }
    }
}

