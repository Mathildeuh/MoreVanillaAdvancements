package fr.mathilde.moreVanillaAdvancements.storage;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerProgressStore {
    // achievementId -> (playerUuid -> progress)
    private final Map<String, Map<UUID, Integer>> map = new HashMap<>();
    private final File file;

    public PlayerProgressStore(File file) {
        this.file = file;
    }

    public void load() {
        map.clear();
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String achId : cfg.getKeys(false)) {
            Map<UUID, Integer> inner = new HashMap<>();
            for (String uuidStr : Objects.requireNonNull(cfg.getConfigurationSection(achId)).getKeys(false)) {
                int v = cfg.getInt(achId + "." + uuidStr, 0);
                try {
                    inner.put(UUID.fromString(uuidStr), v);
                } catch (IllegalArgumentException ignored) {}
            }
            map.put(achId, inner);
        }
    }

    public void save() throws IOException {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, Map<UUID, Integer>> e : map.entrySet()) {
            String ach = e.getKey();
            for (Map.Entry<UUID, Integer> p : e.getValue().entrySet()) {
                cfg.set(ach + "." + p.getKey().toString(), p.getValue());
            }
        }
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        cfg.save(file);
    }

    public int getProgress(String achievementId, UUID uuid) {
        return map.getOrDefault(achievementId, Map.of()).getOrDefault(uuid, 0);
    }

    public void setProgress(String achievementId, UUID uuid, int value) {
        map.computeIfAbsent(achievementId, k -> new HashMap<>()).put(uuid, value);
    }

    public void increment(String achievementId, UUID uuid, int delta) {
        setProgress(achievementId, uuid, getProgress(achievementId, uuid) + delta);
    }
}
