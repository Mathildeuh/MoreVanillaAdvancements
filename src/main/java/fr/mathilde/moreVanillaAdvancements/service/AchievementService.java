package fr.mathilde.moreVanillaAdvancements.service;

import fr.mathilde.moreVanillaAdvancements.lang.LangManager;
import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import fr.mathilde.moreVanillaAdvancements.storage.PlayerProgressStore;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AchievementService {
    private final Map<String, Achievement> all;
    private final PlayerProgressStore store;
    private final LangManager langManager;

    private boolean broadcastChat;
    private boolean showTitle;
    private String chatFormat;

    public AchievementService(Map<String, Achievement> all, PlayerProgressStore store, FileConfiguration cfg, LangManager langManager) {
        this.all = all;
        this.store = store;
        this.langManager = langManager;
        reloadSettings(cfg);
    }

    public void reloadSettings(FileConfiguration cfg) {
        this.broadcastChat = cfg.getBoolean("settings.broadcastChat", true);
        this.showTitle = cfg.getBoolean("settings.showTitle", true);
        this.chatFormat = cfg.getString("settings.chatFormat", "&b{player} &7a complété l'achievement &a{name}");
    }

    public void onEvent(ConditionType type, String target, UUID player) {
        onEventWithAmount(type, target, player, 1);
    }

    public void onEventWithAmount(ConditionType type, String target, UUID player, int amount) {
        for (Achievement a : all.values()) {
            if (a.getType() != type) continue;
            if (!a.getTarget().equals("*") && !a.getTarget().equalsIgnoreCase(target)) continue;
            int cur = store.getProgress(a.getId(), player);
            // Ne pas tracker si l'achievement est déjà complété
            if (cur >= a.getAmount()) continue;
            cur += amount;
            store.setProgress(a.getId(), player, cur);
            if (cur >= a.getAmount()) {
                Player p = Bukkit.getPlayer(player);
                if (p != null) {
                    // Log console
                    Map<String, String> logPlaceholders = new HashMap<>();
                    logPlaceholders.put("player", p.getName());
                    logPlaceholders.put("name", a.getDisplayName());
                    logPlaceholders.put("id", a.getId());
                    Bukkit.getLogger().info(langManager.getMessage("logs.achievement.completed", logPlaceholders));

                    if (showTitle) {
                        Map<String, String> titlePlaceholders = new HashMap<>();
                        titlePlaceholders.put("name", a.getDisplayName());
                        p.sendTitle(
                            langManager.getMessage("achievements.completion.title-main"),
                            langManager.getMessage("achievements.completion.title-sub", titlePlaceholders),
                            10, 60, 10
                        );
                        p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    if (a.getReward() != null) a.getReward().give(p);
                    if (broadcastChat) {
                        broadcastAchievementMessage(p.getName(), a);
                    }
                }
                try { store.save(); } catch (IOException ignored) {}
            }
        }
    }

    private void broadcastAchievementMessage(String playerName, Achievement achievement) {
        // Diviser le format en parties avant et après {name}
        String[] parts = chatFormat.split("\\{name}");
        String before = parts[0].replace("{player}", playerName);
        String after = parts.length > 1 ? parts[1] : "";

        // Créer les composants texte
        TextComponent beforeComp = new TextComponent(ChatColor.translateAlternateColorCodes('&', before));

        // Créer le composant hoverable pour le nom de l'achievement
        TextComponent achievementComp = new TextComponent(ChatColor.GREEN + achievement.getDisplayName());
        TextComponent hoverText = new TextComponent(ChatColor.GRAY + achievement.getDescription());
        achievementComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));

        // Créer le composant pour la partie après
        TextComponent afterComp = new TextComponent(ChatColor.translateAlternateColorCodes('&', after));

        // Combiner et envoyer
        beforeComp.addExtra(achievementComp);
        beforeComp.addExtra(afterComp);

        // Envoyer à tous les joueurs
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(beforeComp);
        }
    }

    public int getProgress(String id, UUID uuid) { return store.getProgress(id, uuid); }

    public Map<String, Achievement> getAll() { return all; }

    public void reset(UUID uuid, String achievementId) {
        store.setProgress(achievementId, uuid, 0);
        try { store.save(); } catch (IOException ignored) {}
    }

    public void resetAll(UUID uuid) {
        for (String id : all.keySet()) {
            store.setProgress(id, uuid, 0);
        }
        try { store.save(); } catch (IOException ignored) {}
    }
}
