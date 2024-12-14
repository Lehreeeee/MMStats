package me.lehreeeee.mmstats.tasks;

import me.lehreeeee.mmstats.managers.MobStatsManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class TempStatRemovalTask extends BukkitRunnable {

    private final MobStatsManager mobStatsManager;
    private final UUID uuid;
    private final String stat;
    private final int value;

    public TempStatRemovalTask(MobStatsManager mobStatsManager, UUID uuid, String stat, int value) {
        this.mobStatsManager = mobStatsManager;
        this.uuid = uuid;
        this.stat = stat;
        this.value = value;
    }

    @Override
    public void run() {
        mobStatsManager.removeTempStat(uuid,stat,value);
    }
}
