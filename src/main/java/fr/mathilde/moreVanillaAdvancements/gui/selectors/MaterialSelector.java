package fr.mathilde.moreVanillaAdvancements.gui.selectors;

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

public class MaterialSelector implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, SelectionData> sessionData = new HashMap<>();
    private static final List<Material> MATERIALS = new ArrayList<>();

    static {
        for (Material m : Material.values()) {
            if (!m.isLegacy() && m.isItem()) {
                MATERIALS.add(m);
            }
        }
        Collections.sort(MATERIALS, Comparator.comparing(Enum::name));
    }

    public MaterialSelector(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openSelector(Player player, Material currentMaterial, SelectionCallback<Material> callback) {
        openPage(player, 0, currentMaterial, callback);
    }

    private void openPage(Player player, int page, Material currentMaterial, SelectionCallback<Material> callback) {
        int itemsPerPage = 45;
        int totalPages = (MATERIALS.size() + itemsPerPage - 1) / itemsPerPage;

        if (page < 0 || page >= totalPages) page = 0;

        Inventory inv = Bukkit.createInventory(player, 54,
            ChatColor.DARK_GRAY + "» " + ChatColor.GOLD + "Select Material (Page " + (page + 1) + "/" + totalPages + ")");

        int startIdx = page * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, MATERIALS.size());

        for (int i = startIdx; i < endIdx; i++) {
            Material m = MATERIALS.get(i);
            ItemStack item = new ItemStack(m);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(m == currentMaterial ? ChatColor.GOLD + "✓ " + m.name() : ChatColor.WHITE + m.name());
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

        String sessionId = player.getUniqueId() + "_mat_" + System.currentTimeMillis();
        sessionData.put(sessionId, new SelectionData(page, currentMaterial, callback));
        player.setMetadata("materialSelector_sessionId", new org.bukkit.metadata.FixedMetadataValue(plugin, sessionId));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (!p.hasMetadata("materialSelector_sessionId")) return;

        String sessionId = p.getMetadata("materialSelector_sessionId").get(0).asString();
        SelectionData data = sessionData.get(sessionId);
        if (data == null) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        ItemStack item = e.getCurrentItem();
        Material mat = item.getType();

        if (mat == Material.ARROW) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.contains("Previous")) {
                p.removeMetadata("materialSelector_sessionId", plugin);
                sessionData.remove(sessionId);
                openPage(p, data.page - 1, data.currentMaterial, data.callback);
            } else if (displayName.contains("Next")) {
                p.removeMetadata("materialSelector_sessionId", plugin);
                sessionData.remove(sessionId);
                openPage(p, data.page + 1, data.currentMaterial, data.callback);
            }
        } else if (mat == Material.BARRIER) {
            p.closeInventory();
            p.removeMetadata("materialSelector_sessionId", plugin);
            sessionData.remove(sessionId);
            data.callback.onCancel();
        } else {
            p.closeInventory();
            p.removeMetadata("materialSelector_sessionId", plugin);
            sessionData.remove(sessionId);
            data.callback.onSelect(mat);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();

        if (!p.hasMetadata("materialSelector_sessionId")) return;

        String sessionId = p.getMetadata("materialSelector_sessionId").get(0).asString();
        SelectionData data = sessionData.get(sessionId);
        if (data != null) {
            sessionData.remove(sessionId);
            p.removeMetadata("materialSelector_sessionId", plugin);
        }
    }

    private static class SelectionData {
        int page;
        Material currentMaterial;
        SelectionCallback<Material> callback;

        SelectionData(int page, Material currentMaterial, SelectionCallback<Material> callback) {
            this.page = page;
            this.currentMaterial = currentMaterial;
            this.callback = callback;
        }
    }
}

