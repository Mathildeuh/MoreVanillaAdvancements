package fr.mathilde.moreVanillaAdvancements.config;

import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.Category;
import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import fr.mathilde.moreVanillaAdvancements.model.Reward;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class AchievementConfig {
    private final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private FileConfiguration configFile;
    private JavaPlugin plugin;

    public Map<String, Achievement> getAchievements() {
        return achievements;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public void load(FileConfiguration cfg) {
        achievements.clear();
        categories.clear();

        // Charger les catégories
        loadCategories(cfg);

        // Charger les achievements
        ConfigurationSection sec = cfg.getConfigurationSection("achievements");
        if (sec == null) return;
        for (String id : sec.getKeys(false)) {
            ConfigurationSection a = sec.getConfigurationSection(id);
            if (a == null) continue;
            String name = a.getString("name", id);
            String description = a.getString("description", "");

        // Charger l'icône (format simple string)
        Material icon = Material.PAPER;

        if (a.isSet("icon")) {
            String iconStr = a.getString("icon", "PAPER");
            Material mat = Material.matchMaterial(iconStr);
            if (mat != null) {
                icon = mat;
            }
        }

            ConditionType type = ConditionType.valueOf(a.getString("type", "BLOCK_BREAK").toUpperCase(Locale.ROOT));
            String target = a.getString("target", "*").toUpperCase(Locale.ROOT);
            int amount = a.getInt("amount", 1);
            String category = a.getString("category", null);
            Reward reward = null;
            if (a.isConfigurationSection("reward")) {
                ConfigurationSection r = a.getConfigurationSection("reward");
                if (r != null) {
                    int xp = r.getInt("xp", 0);
                    String cmd = r.getString("command", "");
                    String give = r.getString("give", "");
                    reward = new Reward(xp, cmd, give);
                }
            }
            if (icon == null) icon = Material.PAPER;
            achievements.put(id, new Achievement(id, name, description, icon, type, target, amount, reward, category));
        }
    }

    private void loadCategories(FileConfiguration cfg) {
        ConfigurationSection catSection = cfg.getConfigurationSection("categories");
        if (catSection == null) return;

        for (String catName : catSection.getKeys(false)) {
            ConfigurationSection cat = catSection.getConfigurationSection(catName);
            if (cat == null) continue;

            String iconStr = cat.getString("icon", "PAPER");
            Material icon = Material.matchMaterial(iconStr);
            if (icon == null) icon = Material.PAPER;

            boolean show = cat.getBoolean("show", true);

            categories.put(catName, new Category(catName, icon, show));
        }
    }

    public void setConfigFile(FileConfiguration cfg, JavaPlugin p) {
        this.configFile = cfg;
        this.plugin = p;
    }

    public void saveAchievement(Achievement achievement, String replacingId) throws IOException {
        if (configFile == null || plugin == null) {
            throw new IOException("Config not initialized");
        }

        // Supprimer l'ancien si on remplace
        if (replacingId != null && !replacingId.equals(achievement.getId())) {
            configFile.set("achievements." + replacingId, null);
            achievements.remove(replacingId);
        }

        // Définir les valeurs
        String path = "achievements." + achievement.getId();
        configFile.set(path + ".name", achievement.getDisplayName());
        configFile.set(path + ".description", achievement.getDescription());

        // Sauvegarder l'icône (format simple)
        configFile.set(path + ".icon", achievement.getIcon().name());

        configFile.set(path + ".type", achievement.getType().name());
        configFile.set(path + ".target", achievement.getTarget());
        configFile.set(path + ".amount", achievement.getAmount());
        if (achievement.getCategory() != null) {
            configFile.set(path + ".category", achievement.getCategory());
        }

        // Ajouter les récompenses si présentes
        if (achievement.getReward() != null && achievement.getReward().hasReward()) {
            Reward reward = achievement.getReward();
            if (reward.getXp() > 0) {
                configFile.set(path + ".reward.xp", reward.getXp());
            }
            if (reward.getGiveItems() != null && !reward.getGiveItems().isEmpty()) {
                configFile.set(path + ".reward.give", reward.getGiveItems());
            }
            if (reward.getCommand() != null && !reward.getCommand().isEmpty()) {
                configFile.set(path + ".reward.command", reward.getCommand());
            }
        }

        // Sauvegarder le fichier
        File configFileOnDisk = new File(plugin.getDataFolder(), "config.yml");
        configFile.save(configFileOnDisk);

        // Mettre à jour en mémoire
        achievements.put(achievement.getId(), achievement);
    }

    public void deleteAchievement(String id) throws IOException {
        if (configFile == null || plugin == null) {
            throw new IOException("Config not initialized");
        }

        configFile.set("achievements." + id, null);
        achievements.remove(id);

        // Sauvegarder le fichier
        File configFileOnDisk = new File(plugin.getDataFolder(), "config.yml");
        configFile.save(configFileOnDisk);
    }
}


