package fr.mathilde.moreVanillaAdvancements.gui;

import fr.mathilde.moreVanillaAdvancements.lang.LangManager;
import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.Category;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AchievementGUI implements Listener {
    private final Map<String, Achievement> all;
    private final Map<String, Category> categories;
    private final AchievementService service;
    private final LangManager langManager;

    // Cache pour stocker le contexte de navigation (viewer -> target info)
    private final Map<UUID, ViewerContext> viewerContexts = new HashMap<>();

    public AchievementGUI(Map<String, Achievement> all, Map<String, Category> categories, AchievementService service, LangManager langManager) {
        this.all = all;
        this.categories = categories;
        this.service = service;
        this.langManager = langManager;
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

        Map<String, String> titlePlaceholders = new HashMap<>();
        titlePlaceholders.put("player", targetName);
        String title = langManager.getMessage("achievements.gui.categories.title", titlePlaceholders);
        Inventory inv = Bukkit.createInventory(viewer, Math.min(size, 54), title);

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
                Map<String, String> namePlaceholders = new HashMap<>();
                namePlaceholders.put("category", catName);
                meta.setDisplayName(langManager.getMessage("achievements.gui.categories.item-name", namePlaceholders));

                List<String> lore = new ArrayList<>();
                for (String line : langManager.getMessageList("achievements.gui.categories.item-lore")) {
                    line = line.replace("{completed}", String.valueOf(completed));
                    line = line.replace("{total}", String.valueOf(achievements.size()));
                    lore.add(line);
                }
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

        Map<String, String> titlePlaceholders = new HashMap<>();
        titlePlaceholders.put("player", targetName);
        titlePlaceholders.put("category", categoryName != null ? categoryName : "");

        String title = categoryName != null
            ? langManager.getMessage("achievements.gui.achievements.title-with-category", titlePlaceholders)
            : langManager.getMessage("achievements.gui.achievements.title-without-category", titlePlaceholders);

        Inventory inv = Bukkit.createInventory(viewer, Math.min(size, 54), title);

        for (Achievement a : achievements) {
            ItemStack it = new ItemStack(a.getIcon() == null ? Material.PAPER : a.getIcon());
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                Map<String, String> namePlaceholders = new HashMap<>();
                namePlaceholders.put("name", a.getDisplayName());
                meta.setDisplayName(langManager.getMessage("achievements.gui.achievements.item-name", namePlaceholders));

                List<String> lore = new ArrayList<>();

                // Description
                Map<String, String> descPlaceholders = new HashMap<>();
                descPlaceholders.put("description", a.getDescription());
                lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.description", descPlaceholders));
                lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.blank-line"));

                // Progression
                int prog = service.getProgress(a.getId(), target);
                Map<String, String> progPlaceholders = new HashMap<>();
                progPlaceholders.put("current", String.valueOf(prog));
                progPlaceholders.put("required", String.valueOf(a.getAmount()));
                lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.progress", progPlaceholders));

                // Afficher si complété
                if (prog >= a.getAmount()) {
                    lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.completed"));
                }

                // Afficher les récompenses si elles existent
                if (a.getReward() != null && a.getReward().hasReward()) {
                    lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.blank-line"));
                    lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.rewards-header"));

                    var reward = a.getReward();

                    // XP
                    if (reward.getXp() > 0) {
                        Map<String, String> xpPlaceholders = new HashMap<>();
                        xpPlaceholders.put("xp", String.valueOf(reward.getXp()));
                        lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.reward-xp", xpPlaceholders));
                    }

                    // Give items
                    if (reward.getGiveItems() != null && !reward.getGiveItems().isEmpty()) {
                        String[] items = reward.getGiveItems().split(",");
                        for (String itemStr : items) {
                            itemStr = itemStr.trim();
                            if (itemStr.contains(":")) {
                                String[] parts = itemStr.split(":");
                                String materialName = parts[0].trim().replace("_", " ").toLowerCase();
                                String qty = parts[1].trim();
                                Map<String, String> itemPlaceholders = new HashMap<>();
                                itemPlaceholders.put("amount", qty);
                                itemPlaceholders.put("item", materialName);
                                lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.reward-item", itemPlaceholders));
                            }
                        }
                    }

                    // Command
                    if (reward.getCommand() != null && !reward.getCommand().isEmpty()) {
                        lore.add(langManager.getMessage("achievements.gui.achievements.item-lore.reward-command"));
                    }
                }

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
                backMeta.setDisplayName(langManager.getMessage("achievements.gui.achievements.back-button.name"));
                back.setItemMeta(backMeta);
            }
            inv.setItem(inv.getSize() - 1, back);
        }

        viewer.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) e.getWhoClicked();

        // Vérifier si le joueur a un contexte actif (= c'est notre GUI)
        ViewerContext context = viewerContexts.get(viewer.getUniqueId());
        if (context == null) return; // Pas notre GUI

        // C'EST NOTRE GUI - Annuler l'événement
        e.setCancelled(true);

        // Ignorer les clics dans l'inventaire du joueur (bas)
        if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) {
            return;
        }

        // Vérifier d'abord si c'est la flèche retour
        if (clicked.getType() == Material.ARROW) {
            openFor(viewer, context.target, context.targetName);
            return;
        }

        // Pour les clics sur les catégories, on exige des métadonnées
        if (!clicked.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) {
            return;
        }

        // Clic sur une catégorie
        String categoryName = ChatColor.stripColor(meta.getDisplayName());

        // Reconstruire la map des catégories
        Map<String, List<Achievement>> byCategory = new LinkedHashMap<>();
        for (Achievement a : all.values()) {
            String cat = a.getCategory() != null ? a.getCategory() : "Général";
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(a);
        }

        // Vérifier si c'est un clic catégorie
        List<Achievement> achievements = byCategory.get(categoryName);
        if (achievements != null) {
            buildAchievementPage(viewer, context.target, context.targetName, achievements, categoryName);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) e.getWhoClicked();

        ViewerContext context = viewerContexts.get(viewer.getUniqueId());
        if (context == null) return; // Pas notre GUI

        // Bloquer TOUS les drags dans notre GUI
        e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();

        // Vérifier si le joueur a un inventaire ouvert (navigation en cours)
        // Ne supprimer le contexte que s'il ferme vraiment l'inventaire
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("MoreVanillaAdvancements"), () -> {
            if (player.getOpenInventory().getType() == org.bukkit.event.inventory.InventoryType.CRAFTING) {
                // L'inventaire fermé est vraiment fermé, supprimer le contexte
                viewerContexts.remove(player.getUniqueId());
            }
        }, 1L);
    }
}
