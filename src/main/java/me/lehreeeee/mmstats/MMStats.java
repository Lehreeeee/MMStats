package me.lehreeeee.mmstats;

import me.lehreeeee.mmstats.commands.MMStatsCommand;
import me.lehreeeee.mmstats.commands.MMStatsCommandTabCompleter;
import me.lehreeeee.mmstats.hooks.PlaceholderAPIHook;
import me.lehreeeee.mmstats.listeners.EntityDamageListener;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MMStats extends JavaPlugin {
    public static MMStats plugin;
    public static boolean debug = false;

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        plugin = this;

        // Save the default config.yml
        saveDefaultConfig();

        MobStatsManager.init(this);
        MobStatsManager mobStatsManager = MobStatsManager.getInstance();
        mobStatsManager.loadMobStats();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook().register();
        }

        getCommand("mmstats").setExecutor(new MMStatsCommand(this));
        getCommand("mmstats").setTabCompleter(new MMStatsCommandTabCompleter());

        new EntityDamageListener(this, mobStatsManager);

        updateDebug();

        logger.info("Enabled MMStats...");
    }

    @Override
    public void onDisable() {
        logger.info("Disabled MMStats...");
    }

    public void updateDebug() {
        debug = this.getConfig().getBoolean("debug",false);
    }
}
