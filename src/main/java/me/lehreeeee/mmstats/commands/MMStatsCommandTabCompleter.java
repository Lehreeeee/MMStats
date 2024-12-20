package me.lehreeeee.mmstats.commands;

import me.lehreeeee.mmstats.managers.MobStatsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MMStatsCommandTabCompleter implements TabCompleter {
    private final List<String> commands = List.of("reload", "stat", "temp");
    private final List<String> availableStats = List.of(
            "damage_reduction", "magic_reduction", "physical_reduction",
            "weapon_reduction", "skill_reduction", "projectile_reduction",
            "unarmed_reduction", "on_hit_reduction", "minion_reduction",
            "dot_reduction", "elements.[elementname]_reduction");
    private final List<String> loadedMobs = new ArrayList<>();
    private final MobStatsManager mobStatsManager;

    public MMStatsCommandTabCompleter(MobStatsManager mobStatsManager){
        this.mobStatsManager = mobStatsManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1){
            return commands;
        }

        if(args[0].equalsIgnoreCase("stat") && args.length == 2){
            loadedMobs.addAll(mobStatsManager.getMobStatsMap().keySet());
            return loadedMobs;
        }

        if(args[0].equalsIgnoreCase("temp")){
            if(args.length == 2) return List.of("UUID");
            if(args.length == 3) return availableStats;
            if(args.length == 4) return List.of("69", "-420");
            if(args.length == 5) return List.of("milliseconds");
        }

        return null;
    }
}
