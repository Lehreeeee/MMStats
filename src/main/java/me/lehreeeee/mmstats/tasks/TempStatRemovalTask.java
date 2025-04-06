package me.lehreeeee.mmstats.tasks;

import me.lehreeeee.mmstats.managers.MobStatsManager;
import org.bukkit.scheduler.BukkitRunnable;

public class TempStatRemovalTask extends BukkitRunnable {

    private final String key;
    private final double value;

    public TempStatRemovalTask(String key, double value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void run() {
        MobStatsManager.getInstance().removeTempStat(key, value);
    }
}
