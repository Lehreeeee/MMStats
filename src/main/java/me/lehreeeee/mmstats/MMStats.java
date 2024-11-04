package me.lehreeeee.mmstats;

import me.lehreeeee.mmstats.commands.MMStatsCommand;
import me.lehreeeee.mmstats.commands.MMStatsCommandTabCompleter;
import me.lehreeeee.mmstats.listeners.EntityDamageListener;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.managers.MythicMobsManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class MMStats extends JavaPlugin {

    private final String debugPrefix = "[MMStats Debug] ";
    private final Logger logger = getLogger();
    private MobStatsManager mobStatsManager;
    private MythicMobsManager mythicMobsManager;

    @Override
    public void onEnable() {

        // Save the default config.yml
        saveDefaultConfig();

        mobStatsManager = new MobStatsManager(this);
        mobStatsManager.loadMobStats();

        getCommand("mmstats").setExecutor(new MMStatsCommand(this, mobStatsManager));
        getCommand("mmstats").setTabCompleter(new MMStatsCommandTabCompleter());

        mythicMobsManager = new MythicMobsManager(this);

        new EntityDamageListener(this, mobStatsManager, mythicMobsManager);

        logger.info("Enabled MMStats...");
    }

    @Override
    public void onDisable() {

        logger.info("Disabled MMStats...");
    }
}
