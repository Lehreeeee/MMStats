package me.lehreeeee.mmstats.tasks;

import me.lehreeeee.mmstats.managers.MobStatsManager;
import org.bukkit.scheduler.BukkitRunnable;

public class TempStatRemovalTask extends BukkitRunnable {

    private final MobStatsManager mobStatsManager;
    private final String key;
    private final double value;

    public TempStatRemovalTask(MobStatsManager mobStatsManager, String key, double value) {
        this.mobStatsManager = mobStatsManager;
        this.key = key;
        this.value = value;
    }

    @Override
    public void run() {
        mobStatsManager.removeTempStat(key, value);
    }
}
