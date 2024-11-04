package me.lehreeeee.mmstats.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.element.Element;
import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.managers.MythicMobsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Optional;

public class EntityDamageListener {

    private final MMStats plugin;
    private final MobStatsManager mobStatsManager;
    private final MythicMobsManager mythicMobsManager;
    private final String debugPrefix;

    public EntityDamageListener(MMStats plugin, MobStatsManager mobStatsManager, MythicMobsManager mythicMobsManager, String debugPrefix){
        this.plugin = plugin;
        this.mobStatsManager = mobStatsManager;
        this.mythicMobsManager = mythicMobsManager;
        this.debugPrefix = debugPrefix;
    };

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAttack(PlayerAttackEvent event) {

        AttackMetadata attack = event.getAttack();
        Entity victim = attack.getTarget();

        // Ignore ded victim
        if (!(victim instanceof LivingEntity)) {
            return;
        }

        // Is it a mythicmobs?
        if(!mythicMobsManager.isMythicMob((LivingEntity) victim)) {
            Bukkit.getLogger().info(debugPrefix+"Victim " + victim.getName() + " is not mythicmobs.");
            return;
        }

        // Imagine having no stat
        if(!mobStatsManager.hasMobStats(mythicMobsManager.getInternalName(victim.getUniqueId()))){
            return;
        }

        DamageMetadata damage = attack.getDamage();

        Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(victim.getUniqueId());
        String internalName = activeMob.get().getType().getInternalName();

        //

        if(internalName.equalsIgnoreCase("v_dummy") && damage.hasElement(Element.valueOf("INA"))) {
//            Bukkit.getLogger().info(debugPrefix+"v_dummy detected, adding 10 ina damage.");
//            damage.add(10, Element.valueOf("INA")); // add 10 weapon-physical damage

            Bukkit.getLogger().info(debugPrefix+"v_dummy detected, doing 50% more ina element damage.");
            damage.multiplicativeModifier(1.5, Element.valueOf("INA")); // increase skill damage by 50%
        }
    }

}
