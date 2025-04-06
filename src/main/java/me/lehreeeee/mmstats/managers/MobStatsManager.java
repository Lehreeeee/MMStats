package me.lehreeeee.mmstats.managers;

import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.tasks.TempStatRemovalTask;
import me.lehreeeee.mmstats.utils.LoggerUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MobStatsManager {
    private static MobStatsManager instance;

    private final Map<String, Map<String, Object>> mobStatsMap = new HashMap<>();
    // Expected structure: (UUID, <"magic_reduction",10>) or (UUID, <"elements.void_reduction",10>)
    private final Map<UUID, Map<String, Double>> mobTempStatsMap = new HashMap<>();
    private final Map<String, TempStatRemovalTask> scheduledTasks = new HashMap<>();
    private final MMStats plugin;

    private MobStatsManager(MMStats plugin){
        this.plugin = plugin;
    }

    public static MobStatsManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MobStatsManager not initialized.");
        }
        return instance;
    }

    public static void init(MMStats plugin) {
        if (instance == null) {
            instance = new MobStatsManager(plugin);
        }
    }

    public void loadMobStats() {
        // Clear existing data if reloading
        mobStatsMap.clear();

        // Get plugin config
        FileConfiguration config = plugin.getConfig();

        if(!config.isConfigurationSection("mythic_mobs")){
            LoggerUtil.warning("\"mythic_mobs\" section not found, will not load any mobstats.");
            return;
        }

        // Get all mobs
        Set<String> mobNames = config.getConfigurationSection("mythic_mobs").getKeys(false);

        // Log all loaded mobs
        LoggerUtil.info("Loaded mobs: " + String.join(", ", mobNames));

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
            double statValue = 0;

            // If its nested stats & elements section
            if (config.isConfigurationSection(path) && path.endsWith(".elements")) {
                Map<String, Double> elementStats = new HashMap<>();
                for (String nestedKey : config.getConfigurationSection(path).getKeys(false)) {
                    statValue = config.getDouble(path + "." + nestedKey,0D);
                    if (statValue != 0 && statValue <= 100)
                        elementStats.put(nestedKey, statValue);
                    else
                        LoggerUtil.warning("Found and ignored unusual stat " + statValue + " at " + path + "." + nestedKey + ", misconfiguration?");
                }
                stats.put(statKey, elementStats);
            } else {
                statValue = config.getDouble(path,0D);
                if (statValue != 0 && statValue <= 100)
                    stats.put(statKey, statValue);
                else
                    LoggerUtil.warning("Found and ignored unusual stat " + statValue + " at " + path + ", misconfiguration?");
            }
        }
        return stats;
    }

    public boolean applyTempStat(UUID uuid, String stat, double value, long ticks, String identifier){
        // Example for /mms temp <target.uuid> damage_reduction 69 420 wind_debuff
        // Can be removed using /mms removetemp <target.uuid> damage_reduction wind_debuff
        // 6f78c9c0-044f-225d-8d01-c8d3cd37638b;damage_reduction;wind_debuff
        stat = stat.toLowerCase();
        identifier = identifier.toLowerCase();
        String key = String.join(";",uuid.toString(), stat, identifier);

        // Is there any existing same buff? Ignore if yes to prevent stacking.
        if(scheduledTasks.containsKey(key)){
            LoggerUtil.debug("Temp stat key " + key + " already exists for this entity!");
            return false;
        }

        mobTempStatsMap.putIfAbsent(uuid, new HashMap<>());
        Map<String,Double> tempStats = mobTempStatsMap.get(uuid);

        // Add the stat or update its value by summing
        tempStats.merge(stat, value, Double::sum);

        // Schedule the temp stat removal
        TempStatRemovalTask task = new TempStatRemovalTask(key, value);
        task.runTaskLater(plugin, ticks);

        // Store it for force cancellation
        scheduledTasks.put(key,task);

        LoggerUtil.debug("Added temp stat " + key + ": " + value);
        return true;
    }

    public void forceRemoveTempStat(String key){
        TempStatRemovalTask task = scheduledTasks.get(key);

        // Force run it
        task.run();

        // Cancel the scheduled task
        task.cancel();
    }

    public void removeTempStat(String key, double value){
        String[] info = key.split(";");
        if(info.length != 3) {
            plugin.getLogger().warning("Error removing temp stats for " + key);
            return;
        }

        UUID uuid = UUID.fromString(info[0]);
        String stat = info[1];
        Map<String,Double> tempStats = mobTempStatsMap.get(uuid);

        if (tempStats != null) {
            // Restore the stat by undoing the temporary change
            double restoredValue = tempStats.get(stat) - value;

            // If the restored value is 0, debuff/buff is gone, remove the stat
            if (restoredValue == 0) {
                LoggerUtil.debug("Restored value is 0, removing from temp stats.");
                tempStats.remove(stat);
                if(tempStats.isEmpty()){
                    LoggerUtil.debug("No more temp stat for this mob, removing from temp stats map.");
                    mobTempStatsMap.remove(uuid);
                }
            } else {
                LoggerUtil.debug("Restored value is " + restoredValue + ", updating temp stats.");
                tempStats.put(stat, restoredValue);
            }
            LoggerUtil.debug("Removed temp stat " + stat + ": " + value + " for " + uuid);
            scheduledTasks.remove(key);
        }
        else LoggerUtil.debug("Failed to remove temp stat " + stat + ": " + value + " for " + uuid);
    }

    public void logMobStats() {
        for (Map.Entry<String, Map<String, Object>> entry : mobStatsMap.entrySet()) {
            String mobName = entry.getKey();
            Map<String, Object> stats = entry.getValue();
            LoggerUtil.info("Stats for " + mobName + ": " + stats);
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

    public boolean hasMobTempStats(UUID uuid) {
        return mobTempStatsMap.containsKey(uuid);
    }

    public boolean hasScheduledTask(String key) {
        return scheduledTasks.containsKey(key);
    }

    public boolean hasMobTempElementalStats(UUID uuid) {
        Map<String,Double> tempStats = mobTempStatsMap.get(uuid);

        if(tempStats != null){
            for (String key : tempStats.keySet()) {
                if (key.startsWith("elements.")) return true;
            }
        }
        return false;
    }

    public Map<String, Double> getMobTempStats(UUID uuid) {
        return mobTempStatsMap.getOrDefault(uuid, new HashMap<>());
    }
}
