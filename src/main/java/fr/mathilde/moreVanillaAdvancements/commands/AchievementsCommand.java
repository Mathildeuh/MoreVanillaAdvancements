package fr.mathilde.moreVanillaAdvancements.commands;

import fr.mathilde.moreVanillaAdvancements.config.AchievementConfig;
import fr.mathilde.moreVanillaAdvancements.config.ConfigValidator;
import fr.mathilde.moreVanillaAdvancements.gui.AchievementGUI;
import fr.mathilde.moreVanillaAdvancements.gui.AdminSettingsGUI;
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

    public AchievementsCommand(AchievementConfig config, AchievementService service, AchievementGUI gui, AdminSettingsGUI adminGui) {
        this.config = config;
        this.service = service;
        this.gui = gui;
        this.adminGui = adminGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                gui.open((Player) sender);
            } else sender.sendMessage(ChatColor.YELLOW + "Utilisation: /" + label + " reload | open [joueur] | view <joueur> | list | reset <joueur> [achievementId|all] | settings");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("mva.reload")) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
                var plugin = sender.getServer().getPluginManager().getPlugin("MoreVanillaAdvancements");
                if (plugin != null) {
                    plugin.getLogger().info("Rechargement de la configuration par " + sender.getName() + "...");
                    plugin.reloadConfig();

                    // Valider la configuration
                    ConfigValidator validator = new ConfigValidator();
                    if (!validator.validate(plugin.getConfig())) {
                        sender.sendMessage(ChatColor.RED + "========== ERREURS DE CONFIGURATION ==========");
                        for (String error : validator.getErrors()) {
                            sender.sendMessage(ChatColor.RED + error);
                        }
                        sender.sendMessage(ChatColor.RED + "Reload annulé. Veuillez corriger la configuration.");
                        plugin.getLogger().warning("Reload annulé à cause d'erreurs de configuration");
                        return true;
                    }

                    // Afficher les avertissements
                    if (!validator.getWarnings().isEmpty()) {
                        sender.sendMessage(ChatColor.YELLOW + "========== AVERTISSEMENTS DE CONFIGURATION ==========");
                        for (String warning : validator.getWarnings()) {
                            sender.sendMessage(ChatColor.YELLOW + warning);
                        }
                    }

                    // Charger la config
                    config.load(plugin.getConfig());
                    service.reloadSettings(plugin.getConfig());

                    plugin.getLogger().info("Configuration rechargée: " + config.getAchievements().size() + " achievements, " + config.getCategories().size() + " catégories");
                    sender.sendMessage(ChatColor.GREEN + "Config rechargée avec succès.");
                } else sender.sendMessage(ChatColor.RED + "Plugin introuvable.");
                return true;
            case "open":
                if (args.length >= 2) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) { sender.sendMessage(ChatColor.RED + "Joueur hors-ligne/non trouvé."); return true; }
                    gui.open(target);
                    sender.sendMessage(ChatColor.GREEN + "GUI ouvert pour " + target.getName());
                } else {
                    if (sender instanceof Player) gui.open((Player) sender);
                    else sender.sendMessage(ChatColor.RED + "Spécifiez un joueur.");
                }
                return true;
            case "view":
                if (!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Command in-game"); return true; }
                if (args.length < 2) { sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " view <joueur>"); return true; }
                Player targetOnline = Bukkit.getPlayerExact(args[1]);
                if (targetOnline != null) {
                    gui.openFor((Player) sender, targetOnline.getUniqueId(), targetOnline.getName());
                    return true;
                }
                @SuppressWarnings("deprecation") OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
                gui.openFor((Player) sender, off.getUniqueId(), off.getName() != null ? off.getName() : args[1]);
                return true;
            case "list":
                sender.sendMessage(ChatColor.GOLD + "===== " + ChatColor.YELLOW + "Achievements disponibles (" + config.getAchievements().size() + ")" + ChatColor.GOLD + " =====");

                // Grouper par catégorie
                Map<String, List<String>> byCategory = new LinkedHashMap<>();
                for (var entry : config.getAchievements().entrySet()) {
                    String achId = entry.getKey();
                    var ach = entry.getValue();
                    String cat = ach.getCategory() != null ? ach.getCategory() : "Général";
                    byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(achId);
                }

                // Afficher par catégorie
                for (Map.Entry<String, List<String>> catEntry : byCategory.entrySet()) {
                    sender.sendMessage(ChatColor.AQUA + "\n▸ " + catEntry.getKey() + ChatColor.GRAY + " (" + catEntry.getValue().size() + ")");

                    for (String achId : catEntry.getValue()) {
                        var ach = config.getAchievements().get(achId);
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
                }

                sender.sendMessage(ChatColor.GOLD + "\nUtilisez " + ChatColor.YELLOW + "/mva open" + ChatColor.GOLD + " pour voir votre progression");
                return true;
            case "reset":
                if (!sender.hasPermission("mva.reset")) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
                if (args.length < 2) { sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " reset <joueur> [achievementId|all]"); return true; }
                @SuppressWarnings("deprecation") OfflinePlayer off2 = Bukkit.getOfflinePlayer(args[1]);

                // Log du reset
                var pluginForLog = sender.getServer().getPluginManager().getPlugin("MoreVanillaAdvancements");
                if (pluginForLog != null) {
                    String resetTarget = args.length >= 3 ? args[2] : "all";
                    pluginForLog.getLogger().info("Reset des achievements par " + sender.getName() + " pour " + args[1] + " (target: " + resetTarget + ")");
                }
                UUID uuid = off2.getUniqueId();
                if (args.length >= 3 && !args[2].equalsIgnoreCase("all")) {
                    String id = args[2];
                    if (!config.getAchievements().containsKey(id)) { sender.sendMessage(ChatColor.RED + "Achievement inconnu."); return true; }
                    service.reset(uuid, id);
                    sender.sendMessage(ChatColor.GREEN + "Progression réinitialisée pour " + id + " de " + (off2.getName() != null ? off2.getName() : args[1]));
                } else {
                    service.resetAll(uuid);
                    sender.sendMessage(ChatColor.GREEN + "Toutes les progressions réinitialisées pour " + (off2.getName() != null ? off2.getName() : args[1]));
                }
                return true;
            case "settings":
                if (!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Command in-game"); return true; }
                if (!sender.hasPermission("mva.reload")) { sender.sendMessage(ChatColor.RED + "No permission."); return true; }
                adminGui.open((Player) sender);
                return true;
            default:
                sender.sendMessage(ChatColor.YELLOW + "Utilisation: /" + label + " reload | open [joueur] | view <joueur> | list | reset <joueur> [achievementId|all] | settings");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList("reload", "open", "view", "list", "reset", "settings");
        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("view"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("reset")) {
            List<String> ids = new ArrayList<>(config.getAchievements().keySet());
            ids.add("all");
            return ids;
        }
        return Collections.emptyList();
    }
}
