package fr.mathilde.moreVanillaAdvancements.lang;

import fr.mathilde.moreVanillaAdvancements.MoreVanillaAdvancements;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangManager {
    private final JavaPlugin plugin;
    private FileConfiguration langConfig;
    private final Map<String, String> messages = new HashMap<>();
    private String currentLang = "en"; // Anglais par défaut

    // Langues disponibles
    private static final List<String> AVAILABLE_LANGUAGES = Arrays.asList("fr", "en", "es");

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Ne copier que le fichier anglais (par défaut) s'il n'existe pas
        File defaultLangFile = new File(plugin.getDataFolder(), "lang_en.yml");
        if (!defaultLangFile.exists()) {
            plugin.saveResource("lang_en.yml", false);
        }

        // Charger la langue depuis la config (par défaut: en)
        String configLang = plugin.getConfig().getString("settings.language", "en");
        loadLang(configLang);
    }

    public void loadLang(String langCode) {
        if (!AVAILABLE_LANGUAGES.contains(langCode)) {
            plugin.getLogger().warning("Langue " + langCode + " non disponible. Utilisation de 'en'.");
            langCode = "en";
        }

        File langFile = new File(plugin.getDataFolder(), "lang_" + langCode + ".yml");

        // Si le fichier n'existe pas, le créer depuis les ressources
        if (!langFile.exists()) {
            try {
                plugin.saveResource("lang_" + langCode + ".yml", false);
                plugin.getLogger().info("Fichier de langue créé: lang_" + langCode + ".yml");
            } catch (Exception e) {
                plugin.getLogger().warning("Impossible de créer le fichier lang_" + langCode + ".yml, utilisation de 'en'.");
                // Notify Bugsnag
                if (MoreVanillaAdvancements.getInstance() != null && MoreVanillaAdvancements.getInstance().getBugsnag() != null) {
                    MoreVanillaAdvancements.getInstance().getBugsnag().notify(e);
                }
                langCode = "en";
                langFile = new File(plugin.getDataFolder(), "lang_en.yml");
            }
        }

        // Charger le fichier
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        messages.clear();
        currentLang = langCode;

        // Charger tous les messages dans le cache
        loadMessagesRecursive("", langConfig);

        plugin.getLogger().info("Langue chargée: " + langCode);
    }

    private void loadMessagesRecursive(String path, org.bukkit.configuration.ConfigurationSection section) {
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (section.isConfigurationSection(key)) {
                loadMessagesRecursive(fullPath, section.getConfigurationSection(key));
            } else if (section.isString(key)) {
                String message = section.getString(key);
                if (message != null) {
                    messages.put(fullPath, ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        }
    }

    /**
     * Récupère un message traduit avec des placeholders
     * @param path Chemin du message (ex: "commands.reload.success")
     * @param placeholders Map des placeholders et leurs valeurs
     * @return Message traduit avec couleurs et placeholders remplacés
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getOrDefault(path, path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    /**
     * Récupère un message traduit sans placeholders
     * @param path Chemin du message
     * @return Message traduit avec couleurs
     */
    public String getMessage(String path) {
        return getMessage(path, null);
    }

    /**
     * Récupère un message avec préfixe
     * @param path Chemin du message
     * @param placeholders Placeholders optionnels
     * @return Message avec préfixe
     */
    public String getMessageWithPrefix(String path, Map<String, String> placeholders) {
        return getMessage("general.prefix") + getMessage(path, placeholders);
    }

    /**
     * Récupère un message avec préfixe sans placeholders
     * @param path Chemin du message
     * @return Message avec préfixe
     */
    public String getMessageWithPrefix(String path) {
        return getMessageWithPrefix(path, null);
    }

    /**
     * Récupère une liste de messages (pour les lores)
     * @param path Chemin de la section
     * @return Liste des messages
     */
    public java.util.List<String> getMessageList(String path) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (langConfig.isList(path)) {
            for (String line : langConfig.getStringList(path)) {
                list.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        return list;
    }

    /**
     * Recharge le fichier de langue
     */
    public void reload() {
        loadLang(currentLang);
    }

    /**
     * Change la langue active
     * @param langCode Code de la langue (fr, en, es)
     * @return true si le changement a réussi
     */
    public boolean changeLang(String langCode) {
        if (!AVAILABLE_LANGUAGES.contains(langCode)) {
            return false;
        }
        loadLang(langCode);

        // Sauvegarder la langue dans config.yml
        plugin.getConfig().set("settings.language", langCode);
        plugin.saveConfig();

        return true;
    }

    /**
     * Retourne la langue actuelle
     * @return Code de la langue actuelle
     */
    public String getCurrentLang() {
        return currentLang;
    }

    /**
     * Retourne la liste des langues disponibles
     * @return Liste des codes de langue
     */
    public List<String> getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }
}
