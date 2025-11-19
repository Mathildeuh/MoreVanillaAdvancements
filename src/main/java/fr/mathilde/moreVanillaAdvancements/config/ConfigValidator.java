package fr.mathilde.moreVanillaAdvancements.config;

import fr.mathilde.moreVanillaAdvancements.MoreVanillaAdvancements;
import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigValidator {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public boolean validate(FileConfiguration cfg) {
        errors.clear();
        warnings.clear();

        // Valider settings
        validateSettings(cfg);

        // Valider catégories
        validateCategories(cfg);

        // Valider achievements
        validateAchievements(cfg);

        return errors.isEmpty();
    }

    private void validateSettings(FileConfiguration cfg) {
        ConfigurationSection settings = cfg.getConfigurationSection("settings");
        if (settings == null) {
            warnings.add("⚠ Section 'settings' manquante, utilisation des valeurs par défaut");
            return;
        }

        // Vérifier les clés booléennes
        if (!settings.isBoolean("broadcastChat")) {
            warnings.add("⚠ settings.broadcastChat doit être un booléen (true/false)");
        }
        if (!settings.isBoolean("showTitle")) {
            warnings.add("⚠ settings.showTitle doit être un booléen (true/false)");
        }

        // Vérifier le format de chat
        String format = settings.getString("chatFormat");
        if (format == null || format.isEmpty()) {
            warnings.add("⚠ settings.chatFormat est vide, utilisation du format par défaut");
        } else if (!format.contains("{name}")) {
            warnings.add("⚠ settings.chatFormat ne contient pas {name}, le nom de l'achievement n'apparaîtra pas");
        } else if (!format.contains("{player}")) {
            warnings.add("⚠ settings.chatFormat ne contient pas {player}, le nom du joueur n'apparaîtra pas");
        }
    }

    private void validateCategories(FileConfiguration cfg) {
        ConfigurationSection catSection = cfg.getConfigurationSection("categories");
        if (catSection == null) {
            warnings.add("⚠ Section 'categories' manquante, les catégories ne s'afficheront pas");
            return;
        }

        for (String catName : catSection.getKeys(false)) {
            ConfigurationSection cat = catSection.getConfigurationSection(catName);
            if (cat == null) {
                warnings.add("⚠ Catégorie '" + catName + "' n'est pas une section valide");
                continue;
            }

            String iconStr = cat.getString("icon");
            if (iconStr == null || iconStr.isEmpty()) {
                warnings.add("⚠ Catégorie '" + catName + "': 'icon' manquante, utilisation de PAPER");
            } else if (Material.matchMaterial(iconStr) == null) {
                warnings.add("⚠ Catégorie '" + catName + "': icon '" + iconStr + "' invalide, utilisation de PAPER");
            }

            if (!cat.isBoolean("show")) {
                warnings.add("⚠ Catégorie '" + catName + "': 'show' doit être un booléen");
            }
        }
    }

    private void validateAchievements(FileConfiguration cfg) {
        ConfigurationSection achievements = cfg.getConfigurationSection("achievements");
        if (achievements == null) {
            errors.add("❌ Section 'achievements' manquante ou vide");
            return;
        }

        if (achievements.getKeys(false).isEmpty()) {
            warnings.add("⚠ Aucun achievement défini");
            return;
        }

        // Collecter les catégories utilisées
        Set<String> usedCategories = new HashSet<>();
        for (String id : achievements.getKeys(false)) {
            ConfigurationSection ach = achievements.getConfigurationSection(id);
            if (ach != null) {
                String category = ach.getString("category");
                if (category != null && !category.isEmpty()) {
                    usedCategories.add(category);
                }
            }
        }

        // Vérifier les catégories définies mais non utilisées
        ConfigurationSection catSection = cfg.getConfigurationSection("categories");
        if (catSection != null) {
            for (String definedCat : catSection.getKeys(false)) {
                if (!usedCategories.contains(definedCat)) {
                    warnings.add("⚠ Catégorie '" + definedCat + "' définie mais non utilisée par aucun achievement");
                }
            }
        }

        // Vérifier les catégories utilisées mais non définies
        Set<String> definedCategories = catSection != null ? catSection.getKeys(false) : new HashSet<>();
        for (String usedCat : usedCategories) {
            if (!definedCategories.contains(usedCat)) {
                warnings.add("⚠ Catégorie '" + usedCat + "' utilisée mais non définie dans la section 'categories'");
            }
        }

        // Valider chaque achievement
        for (String id : achievements.getKeys(false)) {
            validateAchievement(id, achievements.getConfigurationSection(id));
        }
    }

    private void validateAchievement(String id, ConfigurationSection ach) {
        if (ach == null) {
            errors.add("❌ Achievement '" + id + "' n'est pas une section valide");
            return;
        }

        // Valider name
        String name = ach.getString("name");
        if (name == null || name.isEmpty()) {
            errors.add("❌ Achievement '" + id + "': 'name' manquant ou vide");
        }

        // Valider type
        String typeStr = ach.getString("type");
        if (typeStr == null || typeStr.isEmpty()) {
            errors.add("❌ Achievement '" + id + "': 'type' manquant ou vide");
        } else {
            try {
                ConditionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("❌ Achievement '" + id + "': type '" + typeStr + "' invalide. Types valides: " + getValidTypes());
                // Notify Bugsnag for invalid config value
                if (MoreVanillaAdvancements.getInstance() != null && MoreVanillaAdvancements.getInstance().getBugsnag() != null) {
                    MoreVanillaAdvancements.getInstance().getBugsnag().notify(e);
                }
            }
        }

        // Valider amount
        if (!ach.isInt("amount")) {
            errors.add("❌ Achievement '" + id + "': 'amount' doit être un entier");
        } else if (ach.getInt("amount") <= 0) {
            errors.add("❌ Achievement '" + id + "': 'amount' doit être > 0");
        }

        // Valider icon si présent
        if (ach.contains("icon")) {
            String iconStr = ach.getString("icon");
            if (iconStr != null && Material.matchMaterial(iconStr) == null) {
                warnings.add("⚠ Achievement '" + id + "': icon '" + iconStr + "' invalide, utilisation de PAPER");
            }
        }

        // Valider reward si présent
        ConfigurationSection reward = ach.getConfigurationSection("reward");
        if (reward != null) {
            validateReward(id, reward);
        }

        // Warning pour description manquante
        String description = ach.getString("description");
        if (description == null || description.isEmpty()) {
            warnings.add("⚠ Achievement '" + id + "': 'description' manquante ou vide");
        }
    }

    private void validateReward(String achId, ConfigurationSection reward) {
        if (reward.isInt("xp")) {
            int xp = reward.getInt("xp");
            if (xp < 0) {
                warnings.add("⚠ Achievement '" + achId + "': xp négatif");
            }
        }

        if (reward.contains("item")) {
            String itemStr = reward.getString("item");
            if (itemStr != null && !itemStr.isEmpty() && Material.matchMaterial(itemStr) == null) {
                warnings.add("⚠ Achievement '" + achId + "': item '" + itemStr + "' invalide");
            }
        }

        if (reward.isInt("amount")) {
            int amount = reward.getInt("amount");
            if (amount <= 0) {
                warnings.add("⚠ Achievement '" + achId + "': reward.amount doit être > 0");
            }
        }
    }

    private String getValidTypes() {
        StringBuilder sb = new StringBuilder();
        for (ConditionType type : ConditionType.values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(type.name());
        }
        return sb.toString();
    }


    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public void printReport() {
        if (!errors.isEmpty()) {
            System.err.println("\n========== ERREURS DE CONFIGURATION ==========");
            for (String error : errors) {
                System.err.println(error);
            }
        }

        if (!warnings.isEmpty()) {
            System.out.println("\n========== AVERTISSEMENTS DE CONFIGURATION ==========");
            for (String warning : warnings) {
                System.out.println(warning);
            }
        }

        if (errors.isEmpty() && warnings.isEmpty()) {
            System.out.println("✓ Configuration valide!");
        }
    }
}
