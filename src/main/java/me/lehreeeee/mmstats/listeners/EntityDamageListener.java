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
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class EntityDamageListener implements Listener {

    private final MMStats plugin;
    private final Logger logger;
    private final MobStatsManager mobStatsManager;
    private final MythicMobsManager mythicMobsManager;

    public EntityDamageListener(MMStats plugin, MobStatsManager mobStatsManager, MythicMobsManager mythicMobsManager){
        this.plugin = plugin;
        this.mobStatsManager = mobStatsManager;
        this.mythicMobsManager = mythicMobsManager;
        this.logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this,plugin);
    };

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAttack(PlayerAttackEvent event) {

        //logger.info("PlayerAttackEvent Captured.");

        AttackMetadata attack = event.getAttack();
        Entity victim = attack.getTarget();

        // Ignore ded victim
        if (!(victim instanceof LivingEntity)) {
            return;
        }

        // Is it a mythicmobs?
        if(!mythicMobsManager.isMythicMob((LivingEntity) victim)) {
            logger.info("Victim " + victim.getName() + " is not mythicmobs.");
            return;
        }

        // Get internal name of this mob
        String internalName = mythicMobsManager.getInternalName(victim.getUniqueId());

        // Imagine having no stat
        if(!mobStatsManager.hasMobStats(internalName)){
            logger.info("Victim " + victim.getName() + " does not have stats.");
            return;
        }

        // TODO: Check if this damage is mythiclib only and does not include damage from AE or MCMMO
        // Get damage meta data
        DamageMetadata damage = attack.getDamage();

        // Get the stats of the mob
        Map<String, Object> mobStats = mobStatsManager.getMobStats(internalName);

        StringBuilder typeBuilder = new StringBuilder(); // Create a StringBuilder to collect types
        damage.collectTypes().forEach(type -> {
            typeBuilder.append(type).append(", "); // Append each type followed by a comma
        });

        // Remove the last comma and space if there are any types collected
        if (!typeBuilder.isEmpty()) {
            typeBuilder.setLength(typeBuilder.length() - 2); // Remove last comma and space
        }

        // Log all types found in the damage
        logger.info( "Damage Types: " + typeBuilder.toString());

        // TODO: Make sure no negative cooefficient
        if (mobStats.containsKey("damage_reduction")) {
            Integer damageReductionValue = (Integer) mobStats.get("damage_reduction");

            // Check if damage reduction is present and valid
            if (damageReductionValue != null) {
                // Convert to float for calculating modifier
                float damageReduction = damageReductionValue / 100f;

                // Apply the damage reduction modifier
                damage.multiplicativeModifier(1 - damageReduction);

                // Optionally, log the applied modifiers for debugging
                logger.info("Applied damage reduction: " + damageReductionValue + "%, Modifier set to: " + (1 - damageReduction));
            } else {
                logger.warning("Damage reduction stat not found for mob: " + internalName);
            }
        }



//        // Example of how to adjust damage based on stats
//        if (mobStats.containsKey(MobStat.DAMAGE_REDUCTION.getStatName())) {
//            Integer damageReduction = (Integer) mobStats.get(MobStat.DAMAGE_REDUCTION.getStatName());
//            adjustedDamage -= damageReduction; // Apply damage reduction
//        }
//
//        if(damage.hasElement(Element.valueOf("INA"))) {
//            Bukkit.getLogger().info(debugPrefix+"v_dummy detected, adding 10 ina damage.");
//            damage.add(10, Element.valueOf("INA")); // add 10 weapon-physical damage
//
//            Bukkit.getLogger().info(debugPrefix+"v_dummy detected, doing 50% more ina element damage.");
//            damage.multiplicativeModifier(1.5, Element.valueOf("INA")); // increase skill damage by 50%
//        }
    }

}
