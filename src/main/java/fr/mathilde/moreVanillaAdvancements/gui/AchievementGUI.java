package fr.mathilde.moreVanillaAdvancements.gui;

import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.Category;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AchievementGUI implements Listener {
    private final Map<String, Achievement> all;
    private final Map<String, Category> categories;
    private final AchievementService service;

    // Cache pour stocker le contexte de navigation (viewer -> target info)
    private final Map<UUID, ViewerContext> viewerContexts = new HashMap<>();

    public AchievementGUI(Map<String, Achievement> all, Map<String, Category> categories, AchievementService service) {
        this.all = all;
        this.categories = categories;
        this.service = service;
    }

    public void open(Player p) {
        openFor(p, p.getUniqueId(), p.getName());
    }

    public void openFor(Player viewer, UUID target, String targetName) {
        // Sauvegarder le contexte
        viewerContexts.put(viewer.getUniqueId(), new ViewerContext(target, targetName));

        // Group achievements by category
        Map<String, List<Achievement>> byCategory = new LinkedHashMap<>();
        for (Achievement a : all.values()) {
            String cat = a.getCategory() != null ? a.getCategory() : "Général";
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(a);
        }

        // Build first page with category buttons (or list all if only 1 category)
        if (byCategory.size() == 1) {
            List<Achievement> achievements = byCategory.values().iterator().next();
            buildAchievementPage(viewer, target, targetName, achievements, null);
        } else {
            buildCategoryMenu(viewer, target, targetName, byCategory);
        }
    }

    private static class ViewerContext {
        UUID target;
        String targetName;

        ViewerContext(UUID target, String targetName) {
            this.target = target;
            this.targetName = targetName;
        }
    }

    private void buildCategoryMenu(Player viewer, UUID target, String targetName, Map<String, List<Achievement>> byCategory) {
        int size = ((byCategory.size() + 8) / 9) * 9;
        if (size == 0) size = 9;
        Inventory inv = Bukkit.createInventory(viewer, Math.min(size, 54), ChatColor.DARK_GREEN + "Categories - " + targetName);

        for (Map.Entry<String, List<Achievement>> entry : byCategory.entrySet()) {
            String catName = entry.getKey();
            List<Achievement> achievements = entry.getValue();

            // Vérifier si la catégorie doit être visible
            Category cat = categories.get(catName);
            if (cat != null && !cat.isShow()) continue;

            int completed = (int) achievements.stream()
                    .filter(a -> service.getProgress(a.getId(), target) >= a.getAmount())
                    .count();

            // Utiliser l'icône de la catégorie ou par défaut BOOK
            Material icon = (cat != null) ? cat.getIcon() : Material.BOOK;
            ItemStack it = new ItemStack(icon);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + catName);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Achievements: " + completed + "/" + achievements.size());
                lore.add("");
                lore.add(ChatColor.GRAY + "Clique pour ouvrir");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            inv.addItem(it);
        }
        viewer.openInventory(inv);
    }

    private void buildAchievementPage(Player viewer, UUID target, String targetName, List<Achievement> achievements, String categoryName) {
        int size = ((achievements.size() + 8) / 9) * 9;
        if (size == 0) size = 9;
        String title = categoryName != null
            ? ChatColor.DARK_GREEN + categoryName + " - " + targetName
            : ChatColor.DARK_GREEN + "Achievements - " + targetName;
        Inventory inv = Bukkit.createInventory(viewer, Math.min(size, 54), title);

        for (Achievement a : achievements) {
            ItemStack it = new ItemStack(a.getIcon() == null ? Material.PAPER : a.getIcon());
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + a.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + a.getDescription());
                int prog = service.getProgress(a.getId(), target);
                lore.add(ChatColor.YELLOW + "Progression: " + prog + "/" + a.getAmount());
                if (prog >= a.getAmount()) lore.add(ChatColor.GREEN + "COMPLÉTÉ");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            inv.addItem(it);
        }

        // Ajouter un bouton retour si on est dans une catégorie
        if (categoryName != null) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName(ChatColor.YELLOW + "← Retour aux catégories");
                back.setItemMeta(backMeta);
            }
            inv.setItem(inv.getSize() - 1, back);
        }

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();

        // Vérifier si c'est un de nos inventaires
        if (!title.startsWith(ChatColor.DARK_GREEN + "Categories")
            && !title.startsWith(ChatColor.DARK_GREEN + "Achievements")
            && !title.contains(ChatColor.DARK_GREEN.toString())) {
            return;
        }

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        ViewerContext context = viewerContexts.get(viewer.getUniqueId());
        if (context == null) return;

        // Menu des catégories - clic sur une catégorie
        if (title.startsWith(ChatColor.DARK_GREEN + "Categories")) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) return;
            String categoryName = ChatColor.stripColor(meta.getDisplayName());

            // Reconstruire la map des catégories
            Map<String, List<Achievement>> byCategory = new LinkedHashMap<>();
            for (Achievement a : all.values()) {
                String cat = a.getCategory() != null ? a.getCategory() : "Général";
                byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(a);
            }

            List<Achievement> achievements = byCategory.get(categoryName);
            if (achievements != null) {
                buildAchievementPage(viewer, context.target, context.targetName, achievements, categoryName);
            }
        }
        // Page d'achievements - clic sur flèche retour
        else if (clicked.getType() == Material.ARROW) {
            openFor(viewer, context.target, context.targetName);
        }
    }
}
