package fr.mathilde.moreVanillaAdvancements.bstats;

import fr.mathilde.moreVanillaAdvancements.MoreVanillaAdvancements;
import fr.mathilde.moreVanillaAdvancements.config.AchievementConfig;
import fr.mathilde.moreVanillaAdvancements.model.Achievement;
import fr.mathilde.moreVanillaAdvancements.model.Reward;
import fr.mathilde.moreVanillaAdvancements.storage.PlayerProgressStore;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SingleLineChart;

import java.util.*;

/**
 * Manages bStats metrics for MoreVanillaAdvancements plugin.
 * Tracks comprehensive statistics about plugin usage, rewards, and player progression.
 */
public class StatsManager {

    private static final int PLUGIN_ID = 28053;
    private final Metrics metrics;
    private final MoreVanillaAdvancements plugin;

    public StatsManager(MoreVanillaAdvancements plugin) {
        this.plugin = plugin;
        this.metrics = new Metrics(plugin, PLUGIN_ID);
        initializeMetrics();
    }

    /**
     * Initialize all metrics and charts
     */
    private void initializeMetrics() {
        // Configuration statistics
        addAchievementCountChart();
        addCategoryCountChart();
        addAchievementTypesChart();

        // Rewards statistics
        addMostAwardedRewardChart();
        addAchievementsWithRewardsChart();
        addXPRewardDistributionChart();

        // Player progression statistics
        addAverageAchievementsPerPlayerChart();
        addTotalCompletedAchievementsChart();
        addPlayerProgressDistributionChart();

        // Server information
        addServerSoftwareChart();
        addJavaVersionChart();
    }

    /**
     * Track the number of configured achievements
     */
    private void addAchievementCountChart() {
        metrics.addCustomChart(new SingleLineChart("configured_achievements", () -> {
            AchievementConfig config = plugin.getAchievementConfig();
            return config != null ? config.getAchievements().size() : 0;
        }));
    }

    /**
     * Track the number of achievement categories
     */
    private void addCategoryCountChart() {
        metrics.addCustomChart(new SingleLineChart("achievement_categories", () -> {
            AchievementConfig config = plugin.getAchievementConfig();
            return config != null ? config.getCategories().size() : 0;
        }));
    }

    /**
     * Track achievement types distribution
     */
    private void addAchievementTypesChart() {
        metrics.addCustomChart(new SimpleBarChart("achievement_types", () -> {
            Map<String, Integer> map = new HashMap<>();
            AchievementConfig config = plugin.getAchievementConfig();

            if (config != null) {
                config.getAchievements().values().forEach(achievement -> {
                    String type = achievement.getType().name();
                    map.put(type, map.getOrDefault(type, 0) + 1);
                });
            }

            return map;
        }));
    }

    /**
     * Track the most awarded rewards
     */
    private void addMostAwardedRewardChart() {
        metrics.addCustomChart(new SimpleBarChart("most_awarded_rewards", () -> {
            Map<String, Integer> rewardCounts = new HashMap<>();
            AchievementConfig config = plugin.getAchievementConfig();

            if (config != null) {
                config.getAchievements().values().forEach(achievement -> {
                    Reward reward = achievement.getReward();
                    if (reward != null) {
                        String rewardType = categorizeReward(reward);
                        rewardCounts.put(rewardType, rewardCounts.getOrDefault(rewardType, 0) + 1);
                    }
                });
            }

            return rewardCounts;
        }));
    }

    /**
     * Categorize reward types
     */
    private String categorizeReward(Reward reward) {
        StringBuilder sb = new StringBuilder();
        if (reward.getXp() > 0) sb.append("XP ");
        if (reward.getCommand() != null && !reward.getCommand().isEmpty()) sb.append("CMD ");
        if (reward.getGiveItems() != null && !reward.getGiveItems().isEmpty()) sb.append("ITEMS");

        String result = sb.toString().trim();
        return result.isEmpty() ? "None" : result;
    }

    /**
     * Track how many achievements have rewards
     */
    private void addAchievementsWithRewardsChart() {
        metrics.addCustomChart(new SingleLineChart("achievements_with_rewards", () -> {
            AchievementConfig config = plugin.getAchievementConfig();
            if (config == null) return 0;

            return (int) config.getAchievements().values().stream()
                    .filter(a -> a.getReward() != null)
                    .count();
        }));
    }

    /**
     * Track XP reward distribution
     */
    private void addXPRewardDistributionChart() {
        metrics.addCustomChart(new SimpleBarChart("xp_reward_ranges", () -> {
            Map<String, Integer> xpRanges = new HashMap<>();
            AchievementConfig config = plugin.getAchievementConfig();

            if (config != null) {
                config.getAchievements().values().forEach(achievement -> {
                    Reward reward = achievement.getReward();
                    if (reward != null && reward.getXp() > 0) {
                        String range = categorizeXP(reward.getXp());
                        xpRanges.put(range, xpRanges.getOrDefault(range, 0) + 1);
                    }
                });
            }

            return xpRanges;
        }));
    }

    /**
     * Categorize XP rewards into ranges
     */
    private String categorizeXP(int xp) {
        if (xp <= 10) return "0-10 XP";
        if (xp <= 50) return "11-50 XP";
        if (xp <= 100) return "51-100 XP";
        if (xp <= 500) return "101-500 XP";
        return "500+ XP";
    }

    /**
     * Track average achievements completed per player
     */
    private void addAverageAchievementsPerPlayerChart() {
        metrics.addCustomChart(new SingleLineChart("avg_achievements_per_player", () -> {
            PlayerProgressStore store = getPlayerStore();
            AchievementConfig config = plugin.getAchievementConfig();

            if (store == null || config == null) return 0;

            int totalCompleted = 0;
            Set<UUID> uniquePlayers = new HashSet<>();

            for (Achievement achievement : config.getAchievements().values()) {
                // Count unique players who completed this achievement
                for (Map.Entry<String, Map<UUID, Integer>> entry : getProgressMap().entrySet()) {
                    for (Map.Entry<UUID, Integer> playerEntry : entry.getValue().entrySet()) {
                        if (playerEntry.getValue() >= achievement.getAmount()) {
                            totalCompleted++;
                            uniquePlayers.add(playerEntry.getKey());
                        }
                    }
                }
            }

            if (uniquePlayers.isEmpty()) return 0;
            return Math.max(1, totalCompleted / uniquePlayers.size());
        }));
    }

    /**
     * Track total completed achievements across all players
     */
    private void addTotalCompletedAchievementsChart() {
        metrics.addCustomChart(new SingleLineChart("total_completed_achievements", () -> {
            AchievementConfig config = plugin.getAchievementConfig();
            if (config == null) return 0;

            int totalCompleted = 0;
            for (Achievement achievement : config.getAchievements().values()) {
                for (Map.Entry<String, Map<UUID, Integer>> entry : getProgressMap().entrySet()) {
                    for (Integer progress : entry.getValue().values()) {
                        if (progress >= achievement.getAmount()) {
                            totalCompleted++;
                        }
                    }
                }
            }
            return totalCompleted;
        }));
    }

    /**
     * Track player progress distribution (how many completed 0-25%, 25-50%, etc.)
     */
    private void addPlayerProgressDistributionChart() {
        metrics.addCustomChart(new SimpleBarChart("player_completion_percentage", () -> {
            Map<String, Integer> progressRanges = new HashMap<>();
            AchievementConfig config = plugin.getAchievementConfig();

            if (config == null || config.getAchievements().isEmpty()) return progressRanges;

            Set<UUID> allPlayers = new HashSet<>();
            for (Map.Entry<String, Map<UUID, Integer>> entry : getProgressMap().entrySet()) {
                allPlayers.addAll(entry.getValue().keySet());
            }

            for (UUID player : allPlayers) {
                int completedCount = 0;
                for (Achievement achievement : config.getAchievements().values()) {
                    int progress = getPlayerProgress(achievement.getId(), player);
                    if (progress >= achievement.getAmount()) {
                        completedCount++;
                    }
                }

                double percentage = (double) completedCount / config.getAchievements().size() * 100;
                String range = categorizeCompletion(percentage);
                progressRanges.put(range, progressRanges.getOrDefault(range, 0) + 1);
            }

            return progressRanges;
        }));
    }

    /**
     * Categorize completion percentage
     */
    private String categorizeCompletion(double percentage) {
        if (percentage == 0) return "0%";
        if (percentage <= 25) return "1-25%";
        if (percentage <= 50) return "26-50%";
        if (percentage <= 75) return "51-75%";
        return "76-100%";
    }

    /**
     * Track server software distribution
     */
    private void addServerSoftwareChart() {
        metrics.addCustomChart(new SimpleBarChart("server_software", () -> {
            Map<String, Integer> map = new HashMap<>();
            String software = plugin.getServer().getName();
            map.put(software, 1);
            return map;
        }));
    }

    /**
     * Track Java version distribution
     */
    private void addJavaVersionChart() {
        metrics.addCustomChart(new SimpleBarChart("java_version", () -> {
            Map<String, Integer> map = new HashMap<>();
            String version = System.getProperty("java.version");
            String majorVersion = version.split("\\.")[0];
            map.put("Java " + majorVersion, 1);
            return map;
        }));
    }

    // Helper methods
    private PlayerProgressStore getPlayerStore() {
        try {
            java.lang.reflect.Field field = plugin.getClass().getDeclaredField("store");
            field.setAccessible(true);
            return (PlayerProgressStore) field.get(plugin);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Map<UUID, Integer>> getProgressMap() {
        try {
            PlayerProgressStore store = getPlayerStore();
            if (store == null) return new HashMap<>();
            java.lang.reflect.Field field = store.getClass().getDeclaredField("map");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Map<UUID, Integer>> result = (Map<String, Map<UUID, Integer>>) field.get(store);
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private int getPlayerProgress(String achievementId, UUID uuid) {
        PlayerProgressStore store = getPlayerStore();
        if (store == null) return 0;
        return store.getProgress(achievementId, uuid);
    }
}

