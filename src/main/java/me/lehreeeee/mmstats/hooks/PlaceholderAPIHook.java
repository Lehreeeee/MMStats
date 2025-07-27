package me.lehreeeee.mmstats.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.utils.LoggerUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "mmstats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Lehreeeee";
    }

    @Override
    public @NotNull String getVersion() {
        return "69.420.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    /*
        Available Placeholders:
        1. %mmstats_stat_{<uuid>}_{<stat>}% (stat: "magic_reduction" OR "element_void_reduction")
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        // Expecting format: "stat_{<uuid>}_{<stat>}"
        if (params.startsWith("stat_")) {

            // splitParams("{<uuid>}_{<stat>}")
            // output: <uuid>,<stat>
            String[] segments = splitParams(params.substring(5));

            if (segments.length != 2) return "";

            String uuid = segments[0];
            String stat = segments[1].toLowerCase().replace("elements_","elements.");

            try{
                return String.valueOf(MobStatsManager.getInstance().getTotalStat(UUID.fromString(uuid),stat));
            } catch (IllegalArgumentException e) {
                LoggerUtils.severe("Invalid UUID for placeholder: %mmstats_" + params + "%");
                return "";
            }
        }
        return "";
    }

    private String[] splitParams(String params){
        return params.substring(1, params.length() - 1).split("}_\\{");
    }
}
