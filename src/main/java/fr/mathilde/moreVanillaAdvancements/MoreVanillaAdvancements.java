package fr.mathilde.moreVanillaAdvancements;

import fr.mathilde.moreVanillaAdvancements.commands.AchievementsCommand;
import fr.mathilde.moreVanillaAdvancements.config.AchievementConfig;
import fr.mathilde.moreVanillaAdvancements.config.ConfigValidator;
import fr.mathilde.moreVanillaAdvancements.lang.LangManager;
import fr.mathilde.moreVanillaAdvancements.listeners.ProgressListeners;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import fr.mathilde.moreVanillaAdvancements.storage.PlayerProgressStore;
import fr.mathilde.moreVanillaAdvancements.gui.AchievementGUI;
import fr.mathilde.moreVanillaAdvancements.gui.AdminSettingsGUI;
import fr.mathilde.moreVanillaAdvancements.gui.editor.AchievementEditor;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class MoreVanillaAdvancements extends JavaPlugin {

    private static MoreVanillaAdvancements instance;
    private LangManager langManager;
    private AchievementConfig achievementConfig;
    private AchievementService achievementService;
    private AchievementGUI achievementGUI;
    private AdminSettingsGUI adminSettingsGUI;
    private AchievementEditor achievementEditor;
    private PlayerProgressStore store;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        // Initialiser le système de langue
        langManager = new LangManager(this);

        // Valider la configuration
        ConfigValidator validator = new ConfigValidator();
        if (!validator.validate(getConfig())) {
            getLogger().severe(langManager.getMessage("validation.header-errors"));
            for (String error : validator.getErrors()) {
                getLogger().severe(error);
            }
            getLogger().severe(langManager.getMessage("validation.plugin-disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Afficher les avertissements
        if (!validator.getWarnings().isEmpty()) {
            getLogger().warning(langManager.getMessage("validation.header-warnings"));
            for (String warning : validator.getWarnings()) {
                getLogger().warning(warning);
            }
        }

        achievementConfig = new AchievementConfig();
        achievementConfig.load(getConfig());
        achievementConfig.setConfigFile(getConfig(), this);

        // Log du chargement avec messages du lang.yml
        Map<String, String> placeholders = new HashMap<>();

        getLogger().info(langManager.getMessage("logs.startup.separator"));
        placeholders.put("count", String.valueOf(achievementConfig.getAchievements().size()));
        getLogger().info(langManager.getMessage("logs.startup.loading-achievements", placeholders));

        placeholders.put("count", String.valueOf(achievementConfig.getCategories().size()));
        getLogger().info(langManager.getMessage("logs.startup.loading-categories", placeholders));
        getLogger().info(langManager.getMessage("logs.startup.separator"));

        File dataFile = new File(getDataFolder(), "progress.yml");
        store = new PlayerProgressStore(dataFile);
        store.load();
        achievementService = new AchievementService(achievementConfig.getAchievements(), store, getConfig(), langManager);
        achievementGUI = new AchievementGUI(achievementConfig.getAchievements(), achievementConfig.getCategories(), achievementService, langManager);
        adminSettingsGUI = new AdminSettingsGUI(this, achievementService, langManager);
        achievementEditor = new AchievementEditor(this, achievementConfig, langManager);

        getServer().getPluginManager().registerEvents(new ProgressListeners(achievementService), this);
        getServer().getPluginManager().registerEvents(achievementGUI, this);
        getServer().getPluginManager().registerEvents(adminSettingsGUI, this);
        getServer().getPluginManager().registerEvents(achievementEditor, this);

        AchievementsCommand cmd = new AchievementsCommand(achievementConfig, achievementService, achievementGUI, adminSettingsGUI, achievementEditor, langManager);
        if (getCommand("mva") != null) {
            getCommand("mva").setExecutor(cmd);
            getCommand("mva").setTabCompleter(cmd);
        }

        getLogger().info(langManager.getMessage("logs.startup.enabled"));
        placeholders.put("count", String.valueOf(fr.mathilde.moreVanillaAdvancements.model.ConditionType.values().length));
        getLogger().info(langManager.getMessage("logs.startup.types-supported", placeholders));
    }

    @Override
    public void onDisable() {
        try { if (store != null) store.save(); } catch (IOException ignored) {}
    }

    public static MoreVanillaAdvancements getInstance() {
        return instance;
    }

    public AchievementEditor getAchievementEditor() {
        return achievementEditor;
    }

    public LangManager getLangManager() {
        return langManager;
    }
}
