package me.lehreeeee.mmstats.managers;

import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.tasks.TempStatRemovalTask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MobStatsManager {
    private final Map<String, Map<String, Object>> mobStatsMap = new HashMap<>();
    // Expected structure: (UUID, <"magic_reduction",10>) or (UUID, <"elements.void_reduction",10>)
    private final Map<UUID, Map<String, Integer>> mobTempStatsMap = new HashMap<>();
    private final Logger logger;
    private final MMStats plugin;

    public MobStatsManager(MMStats plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadMobStats() {
        // Clear existing data if reloading
        mobStatsMap.clear();

        // Get plugin config
        FileConfiguration config = plugin.getConfig();

        if(!config.isConfigurationSection("mythic_mobs")){
            logger.warning("\"mythic_mobs\" section not found, will not load any mobstats.");
            return;
        }

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
            int statValue = 0;

            // If its nested stats & elements section
            if (config.isConfigurationSection(path) && path.endsWith(".elements")) {
                Map<String, Integer> elementStats = new HashMap<>();
                for (String nestedKey : config.getConfigurationSection(path).getKeys(false)) {
                    statValue = config.getInt(path + "." + nestedKey,0);
                    if (statValue != 0 && statValue <= 100)
                        elementStats.put(nestedKey, statValue);
                    else
                        logger.warning("Found and ignored unusual stat " + statValue + " at " + path + "." + nestedKey + ", misconfiguration?");
                }
                stats.put(statKey, elementStats);
            } else {
                statValue = config.getInt(path,0);
                if (statValue != 0 && statValue <= 100)
                    stats.put(statKey, statValue);
                else
                    logger.warning("Found and ignored unusual stat " + statValue + " at " + path + ", misconfiguration?");
            }
        }
        return stats;
    }

    public void applyTempStat(UUID uuid, String stat, int value, long duration){
        mobTempStatsMap.putIfAbsent(uuid, new HashMap<>());
        Map<String,Integer> tempStats = mobTempStatsMap.get(uuid);

        // Add the stat or update its value by summing
        tempStats.merge(stat, value, Integer::sum);
        long ticks = TimeUnit.MILLISECONDS.toSeconds(duration) * 20;

        // Schedule the temp stat removal
        new TempStatRemovalTask(this, uuid, stat, value).runTaskLater(plugin, ticks);

        debugLogger("Added temp stat " + stat + ": " + value + " for " + uuid);
    }

    public void removeTempStat(UUID uuid, String stat, int value){
        Map<String,Integer> tempStats = mobTempStatsMap.get(uuid);

        if (tempStats != null) {
            // Restore the stat by undoing the temporary change
            int restoredValue = tempStats.get(stat) - value;

            // If the restored value is 0, debuff/buff is gone, remove the stat
            if (restoredValue == 0) {
                tempStats.remove(stat);
            } else {
                tempStats.put(stat, restoredValue);
            }
        }

        debugLogger("Removed temp stat " + stat + ": " + value + " for " + uuid);
    }

    public void logMobStats() {
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

    public Map<String, Map<String, Object>> getMobStatsMap() {
        return mobStatsMap;
    }

    public Map<String, Integer> getMobTempStats(UUID uuid) {
        return mobTempStatsMap.getOrDefault(uuid, new HashMap<>());
    }

    public Map<UUID, Map<String, Integer>> getMobTempStatsMap() {
        return mobTempStatsMap;
    }

    public void debugLogger(String debugMessage){
        if(plugin.getConfig().getBoolean("debug",false))
            logger.info(debugMessage);
    }
}
