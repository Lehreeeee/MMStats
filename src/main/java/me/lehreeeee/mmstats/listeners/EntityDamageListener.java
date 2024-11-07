package me.lehreeeee.mmstats.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
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

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        LivingEntity victim = attack.getTarget();

        // Is it a mythicmobs?
        if(!mythicMobsManager.isMythicMob(victim)) {
            debugLogger("Victim " + victim.getName() + " is not mythicmobs.");
            return;
        }

        // Get internal name of this mob
        String internalName = mythicMobsManager.getInternalName(victim.getUniqueId());

        // Imagine having no stat
        if(!mobStatsManager.hasMobStats(internalName)){
            debugLogger("Victim " + victim.getName() + " does not have stats.");
            return;
        }

        // TODO: Check if this damage is mythiclib only and does not include damage from AE or MCMMO
        // Get damage meta data
        DamageMetadata damage = attack.getDamage();

        // Get the stats of the mob
        Map<String, Object> mobStats = mobStatsManager.getMobStats(internalName);

        // Get all damage types
        Set<DamageType> damageTypes = new HashSet<>(damage.collectTypes());
        // Get all element types
        Set<Element> elementTypes = new HashSet<>(damage.collectElements());

        // Convert to string name of the element for debug logging
        Set<String> elementStrings = elementTypes.stream()
                .map(Element::getName)
                .collect(Collectors.toSet());

        // WTF HOW?!
        if(damageTypes.isEmpty() && elementTypes.isEmpty()) {
            logger.warning("Found unknown damage type to mob: " + internalName);
            return;
        }

        // Log all types found in the damage
        debugLogger( "Damage Types: " + damageTypes);
        debugLogger("Element Types: " + elementStrings);

        // Iterate through all damage types and perform damage reduction

        for(DamageType damageType : damageTypes) {
            String key = damageType.toString().toLowerCase() + "_reduction";

            // Check if the key is valid and present in mobStats
            if (mobStats.containsKey(key)){
                Integer damageReductionValue = (Integer) mobStats.get(key);
                performDamageReduction(damage, damageType, damageReductionValue, internalName);
            }

        }

        if(!elementTypes.isEmpty() && mobStats.containsKey("elements")){
            Object elementsObj = mobStats.get("elements");
            Map<String, Integer> elementStats = new HashMap<>();

            // Cast Obj back to Map
            if(elementsObj instanceof Map){
                try{
                    elementStats = (Map<String, Integer>) elementsObj;
                }
                catch(ClassCastException ex){
                    logger.warning(ex.getMessage());
                }
            }

            // Iterate through all damage types and perform damage reduction
            for (Element elementType : elementTypes) {
                String key = elementType.getName().toLowerCase() + "_reduction";
                // logger.info(String.format("%s - %s - %s", elementType, key, elementStats.get(key)));
                Integer damageReductionValue = elementStats.get(key);
                performDamageReduction(damage, elementType, damageReductionValue, internalName);
            }
        }

        // General damage
        if(mobStats.containsKey("damage_reduction")) {
            Integer damageReductionValue = (Integer) mobStats.get("damage_reduction");
            performDamageReduction(damage, null, damageReductionValue, internalName);
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

    private <T> void performDamageReduction(DamageMetadata damage, T type, Integer damageReductionValue, String internalName){
        // For logging because Element returns io.lumine.mythic.lib.element.Element@12345 without .getName() >:(
        String typeName = (type instanceof Element) ? ((Element) type).getName() : Objects.requireNonNullElse(type, "GENERAL").toString();
        Double originalDamage = damage.getDamage();

        // Do reduction when its not null
        if(damageReductionValue != null) {
            // Convert to float for calculating modifier
            float damageReduction = damageReductionValue / 100f;
            float finalReduction = Math.max(1 - damageReduction, 0);

            switch (type) {
                // Apply the general damage reduction modifier
                case null -> damage.multiplicativeModifier(finalReduction);
                // Apply the other damage reduction modifier
                case DamageType damageType -> damage.multiplicativeModifier(finalReduction, damageType);
                // Apply the elemental damage reduction modifier
                case Element element -> damage.multiplicativeModifier(finalReduction, element);
                default -> logger.warning("Failed to perform damage reduction: Unknown damage type!");
            }

            // Log reduction for debugging
            debugLogger("Applied " + typeName + " reduction: " + damageReductionValue);
            debugLogger("Damage changes: " + originalDamage + " -> " + damage.getDamage());
        }
        else{
            logger.warning("Reduction stat " + typeName + " not found for mob " + internalName);
        }
    }

    public void debugLogger(String debugMessage){
        if(plugin.getConfig().getBoolean("debug",false))
            logger.info(debugMessage);
    }

}
