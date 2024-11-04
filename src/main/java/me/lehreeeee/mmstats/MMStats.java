package me.lehreeeee.mmstats;

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

public final class MMStats extends JavaPlugin {

    private final String debugPrefix = "[MMStats Debug] ";
    private MobStatsManager mobStatsManager;
    private MythicMobsManager mythicMobsManager;

    @Override
    public void onEnable() {

        // Save the default config.yml
        saveDefaultConfig();

        mobStatsManager = new MobStatsManager(this);
        mobStatsManager.loadMobStats();
        mythicMobsManager = new MythicMobsManager(this);

        new EntityDamageListener(this, mobStatsManager, mythicMobsManager, debugPrefix);

        getLogger().info("Enabled MMStats...");
    }

    @Override
    public void onDisable() {

        getLogger().info("Disabled MMStats...");
    }
}
