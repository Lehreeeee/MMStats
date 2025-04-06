package me.lehreeeee.mmstats.commands;

import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.managers.MythicMobsManager;
import me.lehreeeee.mmstats.utils.LoggerUtil;
import me.lehreeeee.mmstats.utils.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class MMStatsCommand implements CommandExecutor {
    private final MMStats plugin;
    private final MobStatsManager mobStatsManager;

    public MMStatsCommand(MMStats plugin) {
        this.plugin = plugin;
        this.mobStatsManager = MobStatsManager.getInstance();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args){
        if(!sender.hasPermission("mms.admin")){
            sendFeedbackMessage(sender,"<#FFA500>Who are you?! You don't have permission to do this!");
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("reload")){
                sendFeedbackMessage(sender,"<#FFA500>Reloading MMStats...");

                // Reload config here
                plugin.reloadConfig();
                plugin.updateDebug();
                // Reload stats from the new config
                mobStatsManager.loadMobStats();

                sendFeedbackMessage(sender,"<#FFA500>Successfully reloaded MMStats.");
                return true;
            }

            if(args[0].equalsIgnoreCase("help")){
                sendCommandUsage(sender);
                return true;
            }
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("stat")){
            Map<String, Object> mobStats = mobStatsManager.getMobStatsMap().get(args[1]);
            if(mobStats == null || mobStats.isEmpty()){
                sendFeedbackMessage(sender,"<#FFA500>Cannot find the stats of this mob.");
                return true;
            }

            sendFeedbackMessage(sender,getMobInfo(mobStats,args[1]));
            return true;
        }

        if(args.length == 4 && args[0].equalsIgnoreCase("removetemp")){
            String key = String.join(";",args[1], args[2], args[3]);
            if(mobStatsManager.hasScheduledTask(key)) {
                mobStatsManager.forceRemoveTempStat(key);
                sendFeedbackMessage(sender,"<#FFA500>Removed temp stat with key " + key);
            }
            else sendFeedbackMessage(sender,"<#FFA500>Failed to remove temp stat from the mob, no existing tempstat with key " + key);
            return true;
        }

        if(args.length == 6 && args[0].equalsIgnoreCase("temp")){

            try{
                UUID uuid = UUID.fromString(args[1]);
                Entity mob = Bukkit.getEntity(uuid);

                if(!(mob instanceof LivingEntity)){
                    sendFeedbackMessage(sender,"<#FFA500>Failed to apply temp stat to the mob.");
                    return true;
                }

                // If its not mythicmob and stat is not weakened
                if(!MythicMobsManager.isMythicMob((LivingEntity) mob) && !args[2].equalsIgnoreCase("weakened")){
                    sendFeedbackMessage(sender,"<#FFA500>Provided mob is not a mythic mob.");
                    return true;
                }

                if(args[5].contains(";")){
                    sendFeedbackMessage(sender,"<#FFA500>Please do not use \";\" in identifier because I am using it as delimiter :suiwheeze:");
                    return true;
                }

                boolean success = mobStatsManager.applyTempStat(UUID.fromString(args[1]), args[2], Double.parseDouble(args[3]), Long.parseLong(args[4]), args[5]);
                if(success) sendFeedbackMessage(sender,"<#FFA500>Successfully applied temp stat to the mob.");
                else sendFeedbackMessage(sender,"<#FFA500>Failed to apply temp stat to the mob, " + args[5] + " already in used.");
                return true;

            } catch (NumberFormatException e) {
                sendFeedbackMessage(sender, "<#FFA500>Invalid stat value or duration. Please check again.");
                return true;
            } catch (IllegalArgumentException e) {
                sendFeedbackMessage(sender, "<#FFA500>Invalid UUID format.");
                return true;
            } catch (Exception e) {
                sendFeedbackMessage(sender, "<#FFA500>An unexpected error occurred.");
                return true;
            }
        }

        sendFeedbackMessage(sender,"<#FFA500>Unknown command. Check /mms help.");
        return true;
    }

    private void sendFeedbackMessage(CommandSender sender, String msg){
        LoggerUtil.info(MessageHelper.getPlainText(msg));

        if (sender instanceof Player) sender.sendMessage(MessageHelper.process(msg,true));
    }

    private void sendCommandUsage(CommandSender sender){
        if (sender instanceof Player) {
            sender.sendMessage(MessageHelper.process("<#FFA500>Command Usage:",true));
            sender.sendMessage(MessageHelper.process("<#FFA500>/mms help <white>-<aqua> Show command usage.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/mms reload <white>-<aqua> Reload mob stats.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/mms stat [Mob name] <white>-<aqua> Show stats of specific mob.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/mms temp [Mob UUID] [Stat name] [Value] [DurationInTicks] [Identifier] <white>-<aqua> Apply temp stat to a mob.",false));
            sender.sendMessage(MessageHelper.process("<#FFA500>/mms removetemp [Mob UUID] [Stat name] [Identifier] <white>-<aqua> Remove temp stat from a mob.",false));
        }
        else{
            LoggerUtil.info("Command Usage:");
            LoggerUtil.info("/mms help - Show command usage.");
            LoggerUtil.info("/mms reload - Reload mob stats.");
            LoggerUtil.info("/mms stat [Mob name] - Show stats of specific mob.");
            LoggerUtil.info("/mms temp [Mob UUID] [Stat name] [DurationInTicks]] [Identifier] - Apply temp stat to a mob.");
            LoggerUtil.info("/mms removetemp [Mob UUID] [Stat name] [Identifier] - Remove temp stat from a mob.");
        }
    }

    private String getMobInfo(Map<String, Object> mobStats, String mobName){
        StringBuilder result = new StringBuilder();
        result.append("<#FFA500>Displaying mob stats for <red>").append(mobName);
        for(Map.Entry<String, Object> entry : mobStats.entrySet()){
            if(entry.getKey().equals("elements") && entry.getValue() instanceof Map<?,?> rawMap){
                try{
                    Map<String, Double> elementStats = (Map<String, Double>) rawMap;

                    for (Map.Entry<String, Double> stat : elementStats.entrySet()) {
                        result.append("<br>")
                                .append("<yellow>")
                                .append(stat.getKey())
                                .append(": ")
                                .append("<aqua>")
                                .append(stat.getValue());
                    }
                }
                catch(ClassCastException ex){
                    LoggerUtil.warning(ex.getMessage());
                }
            }else{
                result.append("<br>")
                        .append("<yellow>")
                        .append(entry.getKey())
                        .append(": ")
                        .append("<aqua>")
                        .append(entry.getValue());
            }
        }

        return result.toString();
    }
}
