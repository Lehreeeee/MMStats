package me.lehreeeee.mmstats.managers;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import me.lehreeeee.mmstats.MMStats;
import org.bukkit.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class MythicMobsManager {
    private final MobExecutor mobManager;

    public MythicMobsManager(MMStats plugin) {
        this.mobManager = MythicBukkit.inst().getMobManager();
    }

    public boolean isMythicMob(LivingEntity entity) {
        return mobManager.isMythicMob(entity);
    }

    public String getInternalName(UUID uuid) {
        Optional<ActiveMob> optActiveMob = mobManager.getActiveMob(uuid);
        return optActiveMob.map(activeMob -> activeMob.getType().getInternalName()).orElse(null);
    }

}
