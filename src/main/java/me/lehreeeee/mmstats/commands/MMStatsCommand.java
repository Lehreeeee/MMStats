package me.lehreeeee.mmstats.commands;

import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MMStatsCommand implements CommandExecutor {
    private final MMStats plugin;
    private final Logger logger;
    private final MobStatsManager mobStatsManager;

    public MMStatsCommand(MMStats plugin, MobStatsManager mobStatsManager) {
        this.plugin = plugin;
        this.mobStatsManager = mobStatsManager;
        this.logger = plugin.getLogger();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(args.length == 1 && args[0].toLowerCase().equals("reload")) {

            sendFeedbackMessage(sender,"Reloading MMStats...");

            // Reload config here
            plugin.reloadConfig();
            // Reload stats from the new config
            mobStatsManager.loadMobStats();

            sendFeedbackMessage(sender,"Successfully reloaded MMStats.");

            return true;

        }

        sendFeedbackMessage(sender,"Unknown command.");
        return false;
    }

    public void sendFeedbackMessage(CommandSender sender, String msg){
        logger.info(msg);
        if (sender instanceof Player) sender.sendMessage(msg);
    }

}
