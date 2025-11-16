package fr.mathilde.moreVanillaAdvancements.config;

import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.Category;
import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import fr.mathilde.moreVanillaAdvancements.model.Reward;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class AchievementConfig {
    private final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();

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
            Material icon = Material.matchMaterial(a.getString("icon", "PAPER"));
            ConditionType type = ConditionType.valueOf(a.getString("type", "BLOCK_BREAK").toUpperCase(Locale.ROOT));
            String target = a.getString("target", "*").toUpperCase(Locale.ROOT);
            int amount = a.getInt("amount", 1);
            String category = a.getString("category", null); // optional
            Reward reward = null;
            if (a.isConfigurationSection("reward")) {
                ConfigurationSection r = a.getConfigurationSection("reward");
                if (r != null) {
                    int xp = r.getInt("xp", 0);
                    Material item = Material.matchMaterial(r.getString("item", ""));
                    int q = r.getInt("amount", 1);
                    String cmd = r.getString("command", "");
                    reward = new Reward(xp, item, q, cmd);
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
}

