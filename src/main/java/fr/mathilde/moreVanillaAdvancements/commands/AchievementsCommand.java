package fr.mathilde.moreVanillaAdvancements.commands;

import fr.mathilde.moreVanillaAdvancements.config.AchievementConfig;
import fr.mathilde.moreVanillaAdvancements.config.ConfigValidator;
import fr.mathilde.moreVanillaAdvancements.gui.AchievementGUI;
import fr.mathilde.moreVanillaAdvancements.gui.AdminSettingsGUI;
import fr.mathilde.moreVanillaAdvancements.gui.editor.AchievementEditor;
import fr.mathilde.moreVanillaAdvancements.lang.LangManager;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class AchievementsCommand implements CommandExecutor, TabCompleter {

    private final AchievementConfig config;
    private final AchievementService service;
    private final AchievementGUI gui;
    private final AdminSettingsGUI adminGui;
    private final AchievementEditor editor;
    private final LangManager langManager;

    public AchievementsCommand(AchievementConfig config, AchievementService service, AchievementGUI gui, AdminSettingsGUI adminGui, AchievementEditor editor, LangManager langManager) {
        this.config = config;
        this.service = service;
        this.gui = gui;
        this.adminGui = adminGui;
        this.editor = editor;
        this.langManager = langManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                gui.open((Player) sender);
            } else sender.sendMessage(langManager.getMessage("commands.usage"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("mva.reload")) {
                    sender.sendMessage(langManager.getMessage("general.no-permission"));
                    return true;
                }
                var plugin = sender.getServer().getPluginManager().getPlugin("MoreVanillaAdvancements");
                if (plugin != null) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", sender.getName());
                    plugin.getLogger().info(langManager.getMessage("commands.reload.reloading", placeholders));
                    plugin.reloadConfig();

                    // Valider la configuration
                    ConfigValidator validator = new ConfigValidator();
                    if (!validator.validate(plugin.getConfig())) {
                        sender.sendMessage(langManager.getMessage("validation.header-errors"));
                        for (String error : validator.getErrors()) {
                            sender.sendMessage(error);
                        }
                        sender.sendMessage(langManager.getMessage("commands.reload.cancelled"));
                        plugin.getLogger().warning(langManager.getMessage("logs.reload.cancelled"));
                        return true;
                    }

                    // Afficher les avertissements
                    if (!validator.getWarnings().isEmpty()) {
                        sender.sendMessage(langManager.getMessage("validation.header-warnings"));
                        for (String warning : validator.getWarnings()) {
                            sender.sendMessage(warning);
                        }
                    }

                    // Charger la config
                    config.load(plugin.getConfig());
                    service.reloadSettings(plugin.getConfig());

                    placeholders.put("achievements", String.valueOf(config.getAchievements().size()));
                    placeholders.put("categories", String.valueOf(config.getCategories().size()));
                    plugin.getLogger().info(langManager.getMessage("logs.reload.loaded", placeholders));
                    sender.sendMessage(langManager.getMessage("commands.reload.success"));
                } else sender.sendMessage(langManager.getMessage("general.plugin-not-found"));
                return true;
            case "open":
                if (args.length >= 2) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(langManager.getMessage("general.player-not-found"));
                        return true;
                    }
                    gui.open(target);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", target.getName());
                    sender.sendMessage(langManager.getMessage("commands.open.success", placeholders));
                } else {
                    if (sender instanceof Player) gui.open((Player) sender);
                    else sender.sendMessage(langManager.getMessage("general.specify-player"));
                }
                return true;
            case "view":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(langManager.getMessage("general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(langManager.getMessage("commands.view.usage"));
                    return true;
                }
                Player targetOnline = Bukkit.getPlayerExact(args[1]);
                if (targetOnline != null) {
                    gui.openFor((Player) sender, targetOnline.getUniqueId(), targetOnline.getName());
                    return true;
                }
                @SuppressWarnings("deprecation") OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
                gui.openFor((Player) sender, off.getUniqueId(), off.getName() != null ? off.getName() : args[1]);
                return true;
            case "list":
                int page = 1;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                }
                showAchievementList(sender, page);
                return true;
            case "reset":
                if (!sender.hasPermission("mva.reset")) {
                    sender.sendMessage(langManager.getMessage("general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(langManager.getMessage("commands.reset.usage"));
                    return true;
                }
                @SuppressWarnings("deprecation") OfflinePlayer off2 = Bukkit.getOfflinePlayer(args[1]);

                // Log du reset
                var pluginForLog = sender.getServer().getPluginManager().getPlugin("MoreVanillaAdvancements");
                if (pluginForLog != null) {
                    String resetTarget = args.length >= 3 ? args[2] : "all";
                    Map<String, String> logPlaceholders = new HashMap<>();
                    logPlaceholders.put("admin", sender.getName());
                    logPlaceholders.put("player", args[1]);
                    logPlaceholders.put("target", resetTarget);
                    pluginForLog.getLogger().info(langManager.getMessage("logs.reset.action", logPlaceholders));
                }
                UUID uuid = off2.getUniqueId();
                if (args.length >= 3 && !args[2].equalsIgnoreCase("all")) {
                    String id = args[2];
                    if (!config.getAchievements().containsKey(id)) {
                        sender.sendMessage(langManager.getMessage("general.no-permission"));
                        return true;
                    }
                    service.reset(uuid, id);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("id", id);
                    placeholders.put("player", off2.getName() != null ? off2.getName() : args[1]);
                    sender.sendMessage(langManager.getMessage("commands.reset.single-success", placeholders));
                } else {
                    service.resetAll(uuid);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", off2.getName() != null ? off2.getName() : args[1]);
                    sender.sendMessage(langManager.getMessage("commands.reset.all-success", placeholders));
                }
                return true;
            case "settings":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(langManager.getMessage("general.player-only"));
                    return true;
                }
                if (!sender.hasPermission("mva.reload")) {
                    sender.sendMessage(langManager.getMessage("general.no-permission"));
                    return true;
                }
                adminGui.open((Player) sender);
                return true;
            case "editor":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(langManager.getMessage("general.player-only"));
                    return true;
                }
                if (!sender.hasPermission("mva.editor")) {
                    sender.sendMessage(langManager.getMessage("general.no-permission"));
                    return true;
                }
                editor.openEditorList((Player) sender);
                return true;
            case "lang":
                if (args.length < 2) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("languages", String.join(", ", langManager.getAvailableLanguages()));
                    sender.sendMessage(langManager.getMessage("commands.lang.usage"));
                    sender.sendMessage(langManager.getMessage("commands.lang.available", placeholders));
                    return true;
                }
                String newLang = args[1].toLowerCase();
                if (langManager.changeLang(newLang)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("lang", newLang);
                    sender.sendMessage(langManager.getMessage("commands.lang.changed", placeholders));

                    // Log
                    var pluginForLang = sender.getServer().getPluginManager().getPlugin("MoreVanillaAdvancements");
                    if (pluginForLang != null) {
                        pluginForLang.getLogger().info("Langue changée en '" + newLang + "' par " + sender.getName());
                    }
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("languages", String.join(", ", langManager.getAvailableLanguages()));
                    sender.sendMessage(langManager.getMessage("commands.lang.invalid", placeholders));
                }
                return true;
            default:
                sender.sendMessage(langManager.getMessage("commands.usage"));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList("reload", "open", "view", "list", "reset", "settings", "editor", "lang");
        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("view"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("lang")) {
            return langManager.getAvailableLanguages();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("reset")) {
            List<String> ids = new ArrayList<>(config.getAchievements().keySet());
            ids.add("all");
            return ids;
        }
        return Collections.emptyList();
    }

    private void showAchievementList(CommandSender sender, int page) {
        final int ACHIEVEMENTS_PER_PAGE = 10;

        // Grouper par catégorie
        Map<String, List<String>> byCategory = new LinkedHashMap<>();
        for (var entry : config.getAchievements().entrySet()) {
            String achId = entry.getKey();
            var ach = entry.getValue();
            String cat = ach.getCategory() != null ? ach.getCategory() : "Général";
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(achId);
        }

        // Aplatir la liste des achievements
        List<String> allAchievements = new ArrayList<>();
        List<String> categoryHeaders = new ArrayList<>();

        for (Map.Entry<String, List<String>> catEntry : byCategory.entrySet()) {
            categoryHeaders.add(catEntry.getKey());
            allAchievements.addAll(catEntry.getValue());
        }

        // Calculer pagination
        int totalPages = (int) Math.ceil((double) allAchievements.size() / ACHIEVEMENTS_PER_PAGE);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * ACHIEVEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ACHIEVEMENTS_PER_PAGE, allAchievements.size());

        // Header
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("count", String.valueOf(config.getAchievements().size()));
        sender.sendMessage(langManager.getMessage("commands.list.header", headerPlaceholders));

        Map<String, String> pageInfoPlaceholders = new HashMap<>();
        pageInfoPlaceholders.put("page", String.valueOf(page));
        pageInfoPlaceholders.put("total", String.valueOf(totalPages));
        sender.sendMessage(langManager.getMessage("commands.list.page-info", pageInfoPlaceholders));

        // Afficher les achievements de la page actuelle
        String currentCategory = null;
        for (int i = startIndex; i < endIndex; i++) {
            String achId = allAchievements.get(i);
            var ach = config.getAchievements().get(achId);
            String cat = ach.getCategory() != null ? ach.getCategory() : "Général";

            // Afficher header de catégorie si changement
            if (!cat.equals(currentCategory)) {
                Map<String, String> catPlaceholders = new HashMap<>();
                catPlaceholders.put("category", cat);
                long catCount = byCategory.get(cat).size();
                catPlaceholders.put("count", String.valueOf(catCount));
                sender.sendMessage(langManager.getMessage("commands.list.category-format", catPlaceholders));
                currentCategory = cat;
            }

            // Afficher achievement
            StringBuilder line = new StringBuilder();
            line.append(ChatColor.YELLOW).append("  • ").append(ChatColor.WHITE).append(ach.getDisplayName());
            line.append(ChatColor.GRAY).append(" (").append(achId).append(")");

            // Afficher la récompense si elle existe
            if (ach.getReward() != null && ach.getReward().hasReward()) {
                line.append(ChatColor.GREEN).append(" → ");
                var reward = ach.getReward();
                List<String> rewards = new ArrayList<>();

                // XP
                if (reward.getXp() > 0) {
                    rewards.add(reward.getXp() + " XP");
                }

                // Give items
                if (reward.getGiveItems() != null && !reward.getGiveItems().isEmpty()) {
                    rewards.add(reward.getGiveItems());
                }

                // Command
                if (reward.getCommand() != null && !reward.getCommand().isEmpty()) {
                    rewards.add("Commande");
                }

                if (!rewards.isEmpty()) {
                    line.append(String.join(ChatColor.GRAY + ", " + ChatColor.GREEN, rewards));
                }
            }

            sender.sendMessage(line.toString());
        }

        // Footer avec navigation
        sender.sendMessage("");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            net.md_5.bungee.api.chat.TextComponent footer = new net.md_5.bungee.api.chat.TextComponent("");

            // Bouton précédent
            if (page > 1) {
                net.md_5.bungee.api.chat.TextComponent prev = new net.md_5.bungee.api.chat.TextComponent(
                    langManager.getMessage("commands.list.pagination.previous")
                );
                prev.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                    "/mva list " + (page - 1)
                ));
                Map<String, String> hoverPlaceholders = new HashMap<>();
                hoverPlaceholders.put("page", String.valueOf(page - 1));
                prev.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new net.md_5.bungee.api.chat.ComponentBuilder(
                        langManager.getMessage("commands.list.pagination.hover-previous", hoverPlaceholders)
                    ).create()
                ));
                footer.addExtra(prev);
            } else {
                footer.addExtra(langManager.getMessage("commands.list.pagination.previous-disabled"));
            }

            footer.addExtra(langManager.getMessage("commands.list.pagination.separator"));

            // Bouton suivant
            if (page < totalPages) {
                net.md_5.bungee.api.chat.TextComponent next = new net.md_5.bungee.api.chat.TextComponent(
                    langManager.getMessage("commands.list.pagination.next")
                );
                next.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                    "/mva list " + (page + 1)
                ));
                Map<String, String> hoverPlaceholders = new HashMap<>();
                hoverPlaceholders.put("page", String.valueOf(page + 1));
                next.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new net.md_5.bungee.api.chat.ComponentBuilder(
                        langManager.getMessage("commands.list.pagination.hover-next", hoverPlaceholders)
                    ).create()
                ));
                footer.addExtra(next);
            } else {
                footer.addExtra(langManager.getMessage("commands.list.pagination.next-disabled"));
            }

            player.spigot().sendMessage(footer);
        } else {
            // Pour la console, afficher simple
            sender.sendMessage(langManager.getMessage("commands.list.pagination.console-help"));
        }

        sender.sendMessage(langManager.getMessage("commands.list.footer"));
    }
}
