package me.lehreeeee.mmstats.hooks;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class MythicMobsHook {
    private static final MobExecutor mobManager = MythicBukkit.inst().getMobManager();

    public static boolean isMythicMob(LivingEntity entity) {
        return mobManager.isMythicMob(entity);
    }

    public static String getInternalName(UUID uuid) {
        return mobManager.getActiveMob(uuid).map(activeMob -> activeMob.getType().getInternalName()).orElse(null);
    }

    public static ActiveMob getActiveMob(UUID uuid){
        return mobManager.getActiveMob(uuid).orElse(null);
    }
}
