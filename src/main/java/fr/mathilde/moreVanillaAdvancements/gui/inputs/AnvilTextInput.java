package fr.mathilde.moreVanillaAdvancements.gui.inputs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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

import java.util.HashMap;
import java.util.Map;

public class AnvilTextInput implements Listener {
    private final JavaPlugin plugin;
    private final Map<String, AnvilInputData> inputData = new HashMap<>();

    public AnvilTextInput(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openInput(Player player, String prompt, String defaultValue, TextInputCallback callback) {
        // Créer un inventaire d'anvil simulé
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.DARK_GRAY + "» " + ChatColor.GOLD + prompt);

        ItemStack paperItem = new ItemStack(Material.PAPER);
        ItemMeta meta = paperItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + (defaultValue.isEmpty() ? prompt : defaultValue));
            paperItem.setItemMeta(meta);
        }

        inv.setItem(0, paperItem);

        // Bouton Confirmer (slot 2)
        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ChatColor.GREEN + "✓ Confirm");
            confirm.setItemMeta(confirmMeta);
        }
        inv.setItem(2, confirm);

        // Bouton Annuler (slot 4)
        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "✗ Cancel");
            cancel.setItemMeta(cancelMeta);
        }
        inv.setItem(4, cancel);

        String sessionId = player.getUniqueId() + "_" + System.currentTimeMillis();
        inputData.put(sessionId, new AnvilInputData(prompt, defaultValue, callback));

        player.openInventory(inv);
        player.setMetadata("anvilInput_sessionId", new org.bukkit.metadata.FixedMetadataValue(plugin, sessionId));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (!p.hasMetadata("anvilInput_sessionId")) return;

        String sessionId = p.getMetadata("anvilInput_sessionId").get(0).asString();
        AnvilInputData data = inputData.get(sessionId);
        if (data == null) return;

        e.setCancelled(true);

        if (e.getSlot() == 2) {
            // Confirm button
            String input = e.getInventory().getItem(0) != null && e.getInventory().getItem(0).hasItemMeta()
                ? e.getInventory().getItem(0).getItemMeta().getDisplayName()
                : data.defaultValue;

            // Remove color codes
            input = input.replaceAll("§[0-9a-fk-or]", "");

            p.closeInventory();
            p.removeMetadata("anvilInput_sessionId", plugin);
            inputData.remove(sessionId);
            data.callback.onInput(input.isEmpty() ? data.defaultValue : input);
        } else if (e.getSlot() == 4) {
            // Cancel button
            p.closeInventory();
            p.removeMetadata("anvilInput_sessionId", plugin);
            inputData.remove(sessionId);
            data.callback.onCancel();
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();

        if (!p.hasMetadata("anvilInput_sessionId")) return;

        String sessionId = p.getMetadata("anvilInput_sessionId").get(0).asString();
        AnvilInputData data = inputData.get(sessionId);
        if (data != null) {
            inputData.remove(sessionId);
            p.removeMetadata("anvilInput_sessionId", plugin);
            data.callback.onCancel();
        }
    }

    private static class AnvilInputData {
        String prompt;
        String defaultValue;
        TextInputCallback callback;

        AnvilInputData(String prompt, String defaultValue, TextInputCallback callback) {
            this.prompt = prompt;
            this.defaultValue = defaultValue;
            this.callback = callback;
        }
    }
}

