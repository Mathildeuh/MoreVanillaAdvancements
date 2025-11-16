package fr.mathilde.moreVanillaAdvancements;

import fr.mathilde.moreVanillaAdvancements.commands.AchievementsCommand;
import fr.mathilde.moreVanillaAdvancements.config.AchievementConfig;
import fr.mathilde.moreVanillaAdvancements.config.ConfigValidator;
import fr.mathilde.moreVanillaAdvancements.listeners.ProgressListeners;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import fr.mathilde.moreVanillaAdvancements.storage.PlayerProgressStore;
import fr.mathilde.moreVanillaAdvancements.gui.AchievementGUI;
import fr.mathilde.moreVanillaAdvancements.gui.AdminSettingsGUI;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class MoreVanillaAdvancements extends JavaPlugin {

    private AchievementConfig achievementConfig;
    private AchievementService achievementService;
    private AchievementGUI achievementGUI;
    private AdminSettingsGUI adminSettingsGUI;
    private PlayerProgressStore store;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        // Valider la configuration
        ConfigValidator validator = new ConfigValidator();
        if (!validator.validate(getConfig())) {
            getLogger().severe(ChatColor.RED + "========== ERREURS DE CONFIGURATION ==========");
            for (String error : validator.getErrors()) {
                getLogger().severe(error);
            }
            getLogger().severe("Le plugin est désactivé à cause des erreurs de configuration.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Afficher les avertissements
        if (!validator.getWarnings().isEmpty()) {
            getLogger().warning(ChatColor.YELLOW + "========== AVERTISSEMENTS DE CONFIGURATION ==========");
            for (String warning : validator.getWarnings()) {
                getLogger().warning(warning);
            }
        }

        achievementConfig = new AchievementConfig();
        achievementConfig.load(getConfig());

        // Log du chargement
        getLogger().info("========================================");
        getLogger().info("Chargement de " + achievementConfig.getAchievements().size() + " achievements");
        getLogger().info("Chargement de " + achievementConfig.getCategories().size() + " catégories");
        getLogger().info("========================================");

        File dataFile = new File(getDataFolder(), "progress.yml");
        store = new PlayerProgressStore(dataFile);
        store.load();
        achievementService = new AchievementService(achievementConfig.getAchievements(), store, getConfig());
        achievementGUI = new AchievementGUI(achievementConfig.getAchievements(), achievementConfig.getCategories(), achievementService);
        adminSettingsGUI = new AdminSettingsGUI(this, achievementService);

        getServer().getPluginManager().registerEvents(new ProgressListeners(achievementService), this);
        getServer().getPluginManager().registerEvents(achievementGUI, this);
        getServer().getPluginManager().registerEvents(adminSettingsGUI, this);

        AchievementsCommand cmd = new AchievementsCommand(achievementConfig, achievementService, achievementGUI, adminSettingsGUI);
        if (getCommand("mva") != null) {
            getCommand("mva").setExecutor(cmd);
            getCommand("mva").setTabCompleter(cmd);
        }

        getLogger().info("✓ MoreVanillaAdvancements activé avec succès!");
        getLogger().info("Types d'achievements supportés: " + fr.mathilde.moreVanillaAdvancements.model.ConditionType.values().length);
    }

    @Override
    public void onDisable() {
        try { if (store != null) store.save(); } catch (IOException ignored) {}
    }
}
