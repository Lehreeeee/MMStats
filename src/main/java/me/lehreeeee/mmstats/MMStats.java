package me.lehreeeee.mmstats;

import me.lehreeeee.mmstats.commands.MMStatsCommand;
import me.lehreeeee.mmstats.commands.MMStatsCommandTabCompleter;
import me.lehreeeee.mmstats.listeners.EntityDamageListener;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.managers.MythicMobsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MMStats extends JavaPlugin {

    private final Logger logger = getLogger();

    private boolean debug = false;

    @Override
    public void onEnable() {

        // Save the default config.yml
        saveDefaultConfig();

        MobStatsManager.init(this);
        MobStatsManager mobStatsManager = MobStatsManager.getInstance();
        mobStatsManager.loadMobStats();

        MythicMobsManager mythicMobsManager = new MythicMobsManager();

        getCommand("mmstats").setExecutor(new MMStatsCommand(this));
        getCommand("mmstats").setTabCompleter(new MMStatsCommandTabCompleter());

        new EntityDamageListener(this, mobStatsManager, mythicMobsManager);

        updateDebug();

        logger.info("Enabled MMStats...");
    }

    @Override
    public void onDisable() {
        logger.info("Disabled MMStats...");
    }

    public void updateDebug() {
        this.debug = this.getConfig().getBoolean("debug",false);
    }

    public static MMStats getPlugin(){
        return (MMStats) Bukkit.getPluginManager().getPlugin("MMStats");
    }

    public boolean shouldPrintDebug(){
        return this.debug;
    }
}
