package me.lehreeeee.mmstats.managers;

import me.lehreeeee.mmstats.MMStats;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class MobStatsManager {
    private final Map<String, Map<String, Object>> mobStatsMap = new HashMap<>();
    private final Logger logger;
    private final MMStats plugin;
    private FileConfiguration config;

    public MobStatsManager(MMStats plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.logger = plugin.getLogger();
    }

    public void loadMobStats() {
        // Clear existing data if reloading
        mobStatsMap.clear();

        // Get all mobs
        Set<String> mobNames = config.getConfigurationSection("mythic_mobs").getKeys(false);

        // Log all loaded mobs
        logger.info("Loaded mobs: " + String.join(", ", mobNames));

        for (String mobName : mobNames) {
            Map<String, Object> stats = loadStats(config, mobName);
            mobStatsMap.put(mobName, stats);
        }

        // Log all loaded mobs & stats
        logMobStats();
    }

    private Map<String, Object> loadStats(FileConfiguration config, String mobName) {
        Map<String, Object> stats = new HashMap<>();
        String basePath = "mythic_mobs." + mobName;

        // Load stats
        for (String statKey : config.getConfigurationSection(basePath).getKeys(false)) {
            String path = basePath + "." + statKey;

            // If its nested stats & elements section
            if (config.isConfigurationSection(path) && path.endsWith(".elements")) {
                Map<String, Integer> elementStats = new HashMap<>();
                for (String nestedKey : config.getConfigurationSection(path).getKeys(false)) {
                    elementStats.put(nestedKey, config.getInt(path + "." + nestedKey));
                }
                stats.put(statKey, elementStats);
            } else {
                stats.put(statKey, config.getInt(path));
            }
        }

        return stats;
    }

    private void logMobStats() {
        for (Map.Entry<String, Map<String, Object>> entry : mobStatsMap.entrySet()) {
            String mobName = entry.getKey();
            Map<String, Object> stats = entry.getValue();
            logger.info("Stats for " + mobName + ": " + stats);
        }
    }

    public boolean hasMobStats(String mobName) {
        return mobStatsMap.containsKey(mobName);
    }

    public Map<String, Object> getMobStats(String mobName) {
        return mobStatsMap.getOrDefault(mobName, new HashMap<>());
    }
}
