package fr.mathilde.moreVanillaAdvancements.gui.selectors;

import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ConditionTypeSelector implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, SelectionData> sessionData = new HashMap<>();

    public ConditionTypeSelector(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSelector(Player player, ConditionType currentType, SelectionCallback<ConditionType> callback) {
        openPage(player, 0, currentType, callback);
    }

    private void openPage(Player player, int page, ConditionType currentType, SelectionCallback<ConditionType> callback) {
        ConditionType[] types = ConditionType.values();
        int itemsPerPage = 45;
        int totalPages = (types.length + itemsPerPage - 1) / itemsPerPage;

        if (page < 0 || page >= totalPages) page = 0;

        Inventory inv = Bukkit.createInventory(player, 54,
            ChatColor.DARK_GRAY + "» " + ChatColor.GOLD + "Select Type (Page " + (page + 1) + "/" + totalPages + ")");

        int startIdx = page * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, types.length);

        for (int i = startIdx; i < endIdx; i++) {
            ConditionType type = types[i];
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(type == currentType ? ChatColor.GOLD + "✓ " + type.name() : ChatColor.WHITE + type.name());
                item.setItemMeta(meta);
            }
            inv.addItem(item);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack prevBtn = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevBtn.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + "« Previous");
                prevBtn.setItemMeta(prevMeta);
            }
            inv.setItem(48, prevBtn);
        }

        if (page < totalPages - 1) {
            ItemStack nextBtn = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextBtn.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.YELLOW + "Next »");
                nextBtn.setItemMeta(nextMeta);
            }
            inv.setItem(50, nextBtn);
        }

        // Cancel button
        ItemStack cancelBtn = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelBtn.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
            cancelBtn.setItemMeta(cancelMeta);
        }
        inv.setItem(49, cancelBtn);

        String sessionId = player.getUniqueId() + "_type_" + System.currentTimeMillis();
        sessionData.put(sessionId, new SelectionData(page, currentType, callback));
        player.setMetadata("typeSelector_sessionId", new org.bukkit.metadata.FixedMetadataValue(plugin, sessionId));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (!p.hasMetadata("typeSelector_sessionId")) return;

        String sessionId = p.getMetadata("typeSelector_sessionId").get(0).asString();
        SelectionData data = sessionData.get(sessionId);
        if (data == null) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        ItemStack item = e.getCurrentItem();
        Material mat = item.getType();

        if (mat == Material.ARROW) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.contains("Previous")) {
                p.removeMetadata("typeSelector_sessionId", plugin);
                sessionData.remove(sessionId);
                openPage(p, data.page - 1, data.currentType, data.callback);
            } else if (displayName.contains("Next")) {
                p.removeMetadata("typeSelector_sessionId", plugin);
                sessionData.remove(sessionId);
                openPage(p, data.page + 1, data.currentType, data.callback);
            }
        } else if (mat == Material.BARRIER) {
            p.closeInventory();
            p.removeMetadata("typeSelector_sessionId", plugin);
            sessionData.remove(sessionId);
            data.callback.onCancel();
        } else {
            // Find selected type by looking at item meta
            String typeName = item.getItemMeta().getDisplayName()
                .replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("✓ ", "");

            try {
                ConditionType selected = ConditionType.valueOf(typeName);
                p.closeInventory();
                p.removeMetadata("typeSelector_sessionId", plugin);
                sessionData.remove(sessionId);
                data.callback.onSelect(selected);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();

        if (!p.hasMetadata("typeSelector_sessionId")) return;

        String sessionId = p.getMetadata("typeSelector_sessionId").get(0).asString();
        SelectionData data = sessionData.get(sessionId);
        if (data != null) {
            sessionData.remove(sessionId);
            p.removeMetadata("typeSelector_sessionId", plugin);
        }
    }

    private static class SelectionData {
        int page;
        ConditionType currentType;
        SelectionCallback<ConditionType> callback;

        SelectionData(int page, ConditionType currentType, SelectionCallback<ConditionType> callback) {
            this.page = page;
            this.currentType = currentType;
            this.callback = callback;
        }
    }
}

