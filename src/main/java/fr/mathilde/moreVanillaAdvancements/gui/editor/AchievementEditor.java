package fr.mathilde.moreVanillaAdvancements.gui.editor;

import fr.mathilde.moreVanillaAdvancements.config.AchievementConfig;
import fr.mathilde.moreVanillaAdvancements.gui.selectors.ConditionTypeSelector;
import fr.mathilde.moreVanillaAdvancements.gui.selectors.SelectionCallback;
import fr.mathilde.moreVanillaAdvancements.lang.LangManager;
import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import fr.mathilde.moreVanillaAdvancements.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AchievementEditor implements Listener {

    private static final int ITEMS_PER_PAGE = 45;
    private static final String LIST_TITLE_PREFIX = ChatColor.DARK_GRAY + "» " + ChatColor.GOLD + "Achievement Editor";
    private static final String EDIT_TITLE = ChatColor.DARK_GRAY + "» " + ChatColor.GOLD + "Edit Achievement";
    private static final String REWARD_TITLE = ChatColor.DARK_GRAY + "» " + ChatColor.GOLD + "Edit Rewards";
    private static final String CONFIRM_DELETE_TITLE = ChatColor.DARK_GRAY + "» " + ChatColor.RED + "Confirm Delete";

    private final JavaPlugin plugin;
    private final AchievementConfig config;
    private final LangManager langManager;
    private final ConditionTypeSelector typeSelector;

    private final Map<UUID, EditorSession> sessions = new HashMap<>();

    public AchievementEditor(JavaPlugin plugin, AchievementConfig config, LangManager langManager) {
        this.plugin = plugin;
        this.config = config;
        this.langManager = langManager;
        this.typeSelector = new ConditionTypeSelector(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // --- Entry Points ---

    public void openEditorList(Player player) {
        openEditorListPage(player, 0);
    }

    private void openEditorListPage(Player player, int page) {
        UUID playerUUID = player.getUniqueId();
        EditorSession session = sessions.get(playerUUID);
        if (session == null) {
            session = new EditorSession();
            sessions.put(playerUUID, session);
            session.achievementIds = new ArrayList<>(config.getAchievements().keySet());
            session.totalPages = (int) Math.ceil((double) session.achievementIds.size() / ITEMS_PER_PAGE);
            if (session.totalPages == 0) session.totalPages = 1;
        }

        page = Math.max(0, Math.min(page, session.totalPages - 1));
        session.currentPage = page;

        String invTitle = LIST_TITLE_PREFIX + " (Page " + (page + 1) + "/" + session.totalPages + ")";
        Inventory inv = Bukkit.createInventory(player, 54, invTitle);

        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, session.achievementIds.size());

        for (int i = startIdx; i < endIdx; i++) {
            String id = session.achievementIds.get(i);
            Achievement a = config.getAchievements().get(id);
            inv.addItem(createAchievementItem(a));
        }

        // Navigation Buttons (textes depuis la langue)
        if (page > 0) {
            inv.setItem(48, createButton(
                Material.ARROW,
                ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.list.previous-page"))
            ));
        }
        if (page < session.totalPages - 1) {
            inv.setItem(50, createButton(
                Material.ARROW,
                ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.list.next-page"))
            ));
        }
        inv.setItem(49, createButton(
            Material.LIME_CONCRETE,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.list.new-button"))
        ));

        player.openInventory(inv);
    }

    // --- Event Handlers ---

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        sessions.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        EditorSession session = sessions.get(playerUUID);

        if (session == null) return;

        // Si le joueur a un callback en attente, traiter le message
        if (session.inputCallback != null) {
            e.setCancelled(true);
            java.util.function.Consumer<String> callback = session.inputCallback;
            session.inputCallback = null; // Nettoyer IMMÉDIATEMENT pour éviter le double traitement

            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(e.getMessage());
            });
            return; // IMPORTANT : sortir après le traitement
        }

        if (session.numberCallback != null) {
            e.setCancelled(true);
            java.util.function.Consumer<String> callback = session.numberCallback;
            session.numberCallback = null; // Nettoyer IMMÉDIATEMENT pour éviter le double traitement

            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(e.getMessage());
            });
            return; // IMPORTANT : sortir après le traitement
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();
        if (!isEditorInventory(title)) return;

        e.setCancelled(true);

        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        if (title.startsWith(LIST_TITLE_PREFIX)) {
            handleListPageClick(e, player, session);
        } else if (title.equals(EDIT_TITLE)) {
            handleEditPageClick(e, player, session);
        } else if (title.equals(REWARD_TITLE)) {
            handleRewardPageClick(e, player, session);
        } else if (title.equals(CONFIRM_DELETE_TITLE)) {
            handleDeleteConfirmClick(e, player, session);
        }
    }

    private void handleListPageClick(InventoryClickEvent e, Player player, EditorSession session) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int slot = e.getSlot();

        // Extraire la page actuelle depuis le titre de l'inventaire pour éviter les décalages
        String title = e.getView().getTitle();
        int currentPageFromTitle = extractPageFromTitle(title);

        // Boutons de navigation (slots fixes)
        if (slot == 48) { // Previous
            openEditorListPage(player, currentPageFromTitle - 1);
            return;
        } else if (slot == 50) { // Next
            openEditorListPage(player, currentPageFromTitle + 1);
            return;
        } else if (slot == 49) { // New
            session.editingId = null;
            session.currentAchievement = new EditorData(null);
            openEditorMenu(player);
            return;
        }

        // Vérifier si c'est un item d'achievement (doit avoir un ID en lore)
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getLore() == null) return;

        String idLine = meta.getLore().stream()
            .filter(line -> ChatColor.stripColor(line).contains("ID:"))
            .findFirst()
            .orElse(null);
        if (idLine == null) return; // Pas un item d'achievement, ignorer

        String achievementId = ChatColor.stripColor(idLine).substring(4).trim();

        if (e.isShiftClick()) {
            confirmDelete(player, achievementId);
        } else {
            openEditorForAchievement(player, achievementId);
        }
    }

    private int extractPageFromTitle(String title) {
        // Format: "§8» §6Achievement Editor (Page X/Y)"
        try {
            String stripped = ChatColor.stripColor(title);
            if (stripped.contains("(Page ")) {
                String pageStr = stripped.split("\\(Page ")[1].split("/")[0];
                return Integer.parseInt(pageStr) - 1; // Convert 1-indexed to 0-indexed
            }
        } catch (Exception ex) {
            // Fallback
        }
        return 0;
    }

    private void handleEditPageClick(InventoryClickEvent e, Player player, EditorSession session) {
        int slot = e.getSlot();
        EditorData data = session.currentAchievement;
        if (data == null) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (slot) {
            case 0: // Name
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.name-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.name-sub")),
                    10, 100, 10
                );
                askPlayerForInput(player, input -> {
                    if (!input.equalsIgnoreCase("cancel") && !input.isEmpty()) {
                        data.name = input;
                    }
                    openEditorMenu(player);
                });
                break;
            case 1: // Description
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.description-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.description-sub")),
                    10, 100, 10
                );
                askPlayerForInput(player, input -> {
                    if (!input.equalsIgnoreCase("cancel") && !input.isEmpty()) {
                        data.description = input;
                    }
                    openEditorMenu(player);
                });
                break;
            case 2: // Icon
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.icon-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.icon-sub")),
                    10, 100, 10
                );
                askPlayerForMaterial(player, material -> {
                    data.icon = material;
                    openEditorMenu(player);
                });
                break;
            case 3: // Type
                player.closeInventory();
                typeSelector.openSelector(player, data.type, new SelectionCallback<>() {
                    @Override public void onSelect(ConditionType selected) { data.type = selected; openEditorMenu(player); }
                    @Override public void onCancel() { openEditorMenu(player); }
                });
                break;
            case 4: // Target
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.target-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.target-sub")),
                    10, 100, 10
                );
                askPlayerForInput(player, input -> {
                    if (!input.equalsIgnoreCase("cancel")) {
                        data.target = input.isEmpty() ? "*" : input.toUpperCase();
                    }
                    openEditorMenu(player);
                });
                break;
            case 5: // Amount
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.amount-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.amount-sub")),
                    10, 100, 10
                );
                askPlayerForNumber(player, number -> {
                    if (number > 0) {
                        data.amount = number;
                    }
                    openEditorMenu(player);
                });
                break;
            case 6: // Category
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.category-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.category-sub")),
                    10, 100, 10
                );
                askPlayerForInput(player, input -> {
                    if (!input.equalsIgnoreCase("cancel")) {
                        data.category = input.isEmpty() ? null : input;
                    }
                    openEditorMenu(player);
                });
                break;
            case 7: // Rewards
                player.closeInventory();
                openRewardEditor(player, data);
                break;
            case 24: // Save
                saveAchievement(player, data, session.editingId);
                break;
            case 26: // Cancel
                sessions.remove(player.getUniqueId());
                openEditorList(player);
                break;
        }
    }

    private void handleRewardPageClick(InventoryClickEvent e, Player player, EditorSession session) {
        int slot = e.getSlot();
        EditorData data = session.currentAchievement;
        if (data == null) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (slot) {
            case 0: // XP
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.reward-xp-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.reward-xp-sub")),
                    10, 100, 10
                );
                askPlayerForNumber(player, number -> {
                    if (number >= 0) {
                        if (data.reward == null) {
                            data.reward = new RewardData(number, "", "");
                        } else {
                            data.reward.xp = number;
                        }
                    }
                    openRewardEditor(player, data);
                });
                break;
            case 1: // Give Items
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.reward-give-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.reward-give-sub")),
                    10, 100, 10
                );
                askPlayerForInput(player, input -> {
                    if (!input.equalsIgnoreCase("cancel")) {
                        if (data.reward == null) {
                            data.reward = new RewardData(0, "", input);
                        } else {
                            data.reward.give = input;
                        }
                    }
                    openRewardEditor(player, data);
                });
                break;
            case 2: // Command
                player.closeInventory();
                player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.reward-command-main")),
                    ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.titles.reward-command-sub")),
                    10, 100, 10
                );
                askPlayerForInput(player, input -> {
                    if (!input.equalsIgnoreCase("cancel")) {
                        if (data.reward == null) {
                            data.reward = new RewardData(0, input, "");
                        } else {
                            data.reward.command = input;
                        }
                    }
                    openRewardEditor(player, data);
                });
                break;
            case 26: // Back
                player.closeInventory();
                openEditorMenu(player);
                break;
        }
    }

    // --- Sub-Menus ---

    private void openEditorForAchievement(Player player, String achievementId) {
        Achievement achievement = config.getAchievements().get(achievementId);
        if (achievement == null) {
            player.sendMessage(langManager.getMessage("editor.achievement-not-found"));
            return;
        }

        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }
        session.editingId = achievementId;
        session.currentAchievement = new EditorData(achievement);

        openEditorMenu(player);
    }

    private void openEditorMenu(Player player) {
        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null || session.currentAchievement == null) return;

        EditorData data = session.currentAchievement;
        Inventory inv = Bukkit.createInventory(player, 27, EDIT_TITLE);

        inv.setItem(0, createButton(
            Material.NAME_TAG,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.name")),
            data.name
        ));
        inv.setItem(1, createButton(
            Material.PAPER,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.description")),
            data.description
        ));
        inv.setItem(2, createButton(
            data.icon,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.icon")),
            data.icon.name()
        ));
        inv.setItem(3, createButton(
            Material.COMPASS,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.type")),
            data.type.name()
        ));
        inv.setItem(4, createButton(
            Material.CROSSBOW,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.target")),
            data.target
        ));
        inv.setItem(5, createButton(
            Material.REPEATER,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.amount")),
            String.valueOf(data.amount)
        ));
        inv.setItem(6, createButton(
            Material.BOOK,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.category")),
            data.category == null ? "None" : data.category
        ));
        inv.setItem(7, createButton(
            Material.CHEST,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.rewards")),
            buildRewardLore(data.reward)
        ));

        inv.setItem(24, createButton(
            Material.LIME_CONCRETE,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.save"))
        ));
        inv.setItem(26, createButton(
            Material.RED_CONCRETE,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.edit.cancel"))
        ));

        player.openInventory(inv);
    }

    private void openRewardEditor(Player player, EditorData data) {
        Inventory inv = Bukkit.createInventory(player, 27, REWARD_TITLE);

        inv.setItem(0, createButton(
            Material.EXPERIENCE_BOTTLE,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.rewards.xp")),
            String.valueOf(data.reward != null ? data.reward.xp : 0)
        ));
        inv.setItem(1, createButton(
            Material.CHEST,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.rewards.give")),
            data.reward != null && data.reward.give != null ? data.reward.give : "None"
        ));
        inv.setItem(2, createButton(
            Material.COMMAND_BLOCK,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.rewards.command")),
            data.reward != null && data.reward.command != null ? data.reward.command : "None"
        ));

        inv.setItem(26, createButton(
            Material.ARROW,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.rewards.back"))
        ));

        player.openInventory(inv);
    }

    private void confirmDelete(Player player, String achievementId) {
        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        session.deleteId = achievementId;

        Inventory inv = Bukkit.createInventory(player, 27, CONFIRM_DELETE_TITLE);

        Achievement a = config.getAchievements().get(achievementId);
        if (a != null) {
            inv.setItem(9, createButton(a.getIcon() == null ? Material.PAPER : a.getIcon(), ChatColor.GOLD + a.getDisplayName()));
        }

        inv.setItem(11, createButton(
            Material.RED_CONCRETE,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.delete.confirm"))
        ));
        inv.setItem(15, createButton(
            Material.LIME_CONCRETE,
            ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.delete.cancel"))
        ));

        player.openInventory(inv);
    }

    private void handleDeleteConfirmClick(InventoryClickEvent e, Player player, EditorSession session) {
        int slot = e.getSlot();

        if (slot == 11) { // Confirm delete
            try {
                config.deleteAchievement(session.deleteId);
                player.sendMessage(langManager.getMessage("editor.deleted-success"));
                sessions.remove(player.getUniqueId());
                openEditorList(player);
            } catch (Exception ex) {
                player.sendMessage(langManager.getMessage("editor.delete-error"));
                plugin.getLogger().warning("Error deleting achievement: " + ex.getMessage());
            }
        } else if (slot == 15) { // Cancel delete
            openEditorListPage(player, session.currentPage);
        }
    }

    // --- Utility Methods ---

    private void askPlayerForInput(Player player, java.util.function.Consumer<String> callback) {
        EditorSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.inputCallback = input -> {
                if (input.equalsIgnoreCase("cancel")) {
                    openEditorMenu(player);
                } else {
                    callback.accept(input);
                }
            };
        }
    }

    private void askPlayerForNumber(Player player, java.util.function.Consumer<Integer> callback) {
        EditorSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.numberCallback = input -> {
                if (input.equalsIgnoreCase("cancel")) {
                    openEditorMenu(player);
                } else {
                    try {
                        int number = Integer.parseInt(input);
                        callback.accept(number);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED + "Invalid number! Please try again.");
                        // Pas de re-prompt texte, le title reste visible le temps du prompt
                    }
                }
            };
        }
    }

    private void askPlayerForMaterial(Player player, java.util.function.Consumer<Material> callback) {
        EditorSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.inputCallback = input -> {
                if (input.equalsIgnoreCase("cancel")) {
                    openEditorMenu(player);
                } else if (input.equalsIgnoreCase("hand")) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand.getType() == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "You don't have an item in your hand!");
                    } else {
                        callback.accept(itemInHand.getType());
                    }
                } else {
                    try {
                        Material material = Material.valueOf(input.toUpperCase());
                        callback.accept(material);
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Material not found! Please try again.");
                    }
                }
            };
        }
    }

    private void saveAchievement(Player player, EditorData data, String existingId) {
        if (data.name.isEmpty()) {
            player.sendMessage(langManager.getMessage("editor.name-required"));
            return;
        }

        String id = existingId;
        if (id == null) {
            id = data.name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            int counter = 1;
            String baseId = id;
            while (config.getAchievements().containsKey(id)) {
                id = baseId + "_" + counter++;
            }
        }

        Reward reward = (data.reward != null && data.reward.hasReward())
            ? new Reward(data.reward.xp, data.reward.command, data.reward.give)
            : null;

        Achievement achievement = new Achievement(id, data.name, data.description, data.icon, data.type, data.target, data.amount, reward, data.category);

        try {
            config.saveAchievement(achievement, existingId);
            player.sendMessage(langManager.getMessage("editor.saved-success"));
        } catch (Exception ex) {
            player.sendMessage(langManager.getMessage("editor.save-error"));
            plugin.getLogger().warning("Error saving achievement: " + ex.getMessage());
        } finally {
            sessions.remove(player.getUniqueId());
            openEditorList(player);
        }
    }

    private boolean isEditorInventory(String title) {
        return title.startsWith(LIST_TITLE_PREFIX) || title.equals(EDIT_TITLE) || title.equals(REWARD_TITLE) || title.equals(CONFIRM_DELETE_TITLE);
    }

    private String[] buildRewardLore(RewardData reward) {
        if (reward == null) {
            return new String[]{"None"};
        }
        List<String> lore = new ArrayList<>();
        if (reward.xp > 0) lore.add("XP: " + reward.xp);
        if (reward.give != null && !reward.give.isEmpty()) lore.add("Items: " + reward.give);
        if (reward.command != null && !reward.command.isEmpty()) lore.add("Command");
        return lore.isEmpty() ? new String[]{"None"} : lore.toArray(new String[0]);
    }

    private ItemStack createAchievementItem(Achievement a) {
        ItemStack item = new ItemStack(a.getIcon() == null ? Material.PAPER : a.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + a.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "ID: " + a.getId());
            lore.add(ChatColor.GRAY + "Type: " + a.getType().name());
            lore.add("");
            lore.add(ChatColor.YELLOW + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.list.item-lore-edit"))));
            lore.add(ChatColor.RED + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.list.item-lore-delete"))));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> finalLore = new ArrayList<>();
            for (String line : lore) {
                if (line == null || line.isEmpty()) continue;
                finalLore.add(ChatColor.GRAY + line);
            }
            if (finalLore.isEmpty() && !name.contains("Page") && !name.contains("Save") && !name.contains("Cancel")) {
                // Ajout d'une ligne "clic pour éditer" pour les boutons simples
                finalLore.add(ChatColor.YELLOW + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', langManager.getMessage("editor.list.item-lore-edit"))));
            }
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }
        return item;
    }


    // --- Inner Classes ---

    static class EditorSession {
        List<String> achievementIds;
        String editingId;
        String deleteId;
        EditorData currentAchievement;
        int currentPage = 0;
        int totalPages = 1;
        java.util.function.Consumer<String> inputCallback;
        java.util.function.Consumer<String> numberCallback;
    }

    static class EditorData {
        String name;
        String description;
        Material icon;
        ConditionType type;
        String target;
        int amount;
        String category;
        RewardData reward;

        EditorData(Achievement existing) {
            if (existing != null) {
                this.name = existing.getDisplayName();
                this.description = existing.getDescription();
                this.icon = existing.getIcon();
                this.type = existing.getType();
                this.target = existing.getTarget();
                this.amount = existing.getAmount();
                this.category = existing.getCategory();
                if (existing.getReward() != null) {
                    this.reward = new RewardData(
                        existing.getReward().getXp(),
                        existing.getReward().getCommand(),
                        existing.getReward().getGiveItems()
                    );
                }
            } else { // Defaults for new achievement
                this.name = "";
                this.description = "";
                this.icon = Material.PAPER;
                this.type = ConditionType.BLOCK_BREAK;
                this.target = "*";
                this.amount = 1;
                this.category = null;
                this.reward = null;
            }
        }
    }

    static class RewardData {
        int xp;
        String command;
        String give;

        RewardData(int xp, String command, String give) {
            this.xp = xp;
            this.command = command;
            this.give = give;
        }

        boolean hasReward() {
            return xp > 0 || (command != null && !command.isEmpty()) || (give != null && !give.isEmpty());
        }
    }
}

