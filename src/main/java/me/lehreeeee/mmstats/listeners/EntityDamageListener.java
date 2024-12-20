package me.lehreeeee.mmstats.listeners;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.managers.MythicMobsManager;
import org.bukkit.Bukkit;
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
        AttackMetadata attack = event.getAttack();
        LivingEntity victim = attack.getTarget();
        UUID uuid = victim.getUniqueId();

        // Is it a mythicmobs?
        if(!mythicMobsManager.isMythicMob(victim)) {
            debugLogger("Victim " + victim.getName() + " is not mythicmobs.");
            return;
        }

        // Get internal name of this mob
        String internalName = mythicMobsManager.getInternalName(uuid);

        // Imagine having no stat
        if(!mobStatsManager.hasMobStats(internalName) && !mobStatsManager.hasMobTempStats(uuid)){
            debugLogger("Victim " + victim.getName() + " does not have stats.");
            return;
        }

        // Get damage meta data
        DamageMetadata damage = attack.getDamage();

        // Get the stats of the mob
        Map<String, Object> mobStats = mobStatsManager.getMobStats(internalName);
        Map<String, Integer> mobTempStats = mobStatsManager.getMobTempStats(uuid);

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
        debugLogger( "Damage Types: " + damageTypes + " Element Types: " + elementStrings);

        // Iterate through all damage types and perform reduction
        for(DamageType damageType : damageTypes) {
            String key = damageType.toString().toLowerCase() + "_reduction";
            Integer damageReductionValue = 0;

            // Set base stat if exists
            if(mobStats.containsKey(key)) damageReductionValue = (Integer) mobStats.get(key);

            // Calculate final reduction value with temp stat
            if(mobTempStats.containsKey(key)) damageReductionValue += mobTempStats.get(key);

            modifyDamage(damage, damageType, damageReductionValue, internalName);

        }

        // Iterate through all elemental damage types and perform reduction
        if(!elementTypes.isEmpty() && (mobStats.containsKey("elements") || mobStatsManager.hasMobTempElementalStats(uuid))){
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
                Integer damageReductionValue = 0;

                // Set base stat if exists
                if (elementStats.containsKey(key)) damageReductionValue = elementStats.getOrDefault(key,0);

                // Calculate final reduction value with temp stat
                if(mobTempStats.containsKey("elements." + key)) damageReductionValue += mobTempStats.get("elements." + key);

                modifyDamage(damage, elementType, damageReductionValue, internalName);
            }
        }

        // General damage
        if(mobStats.containsKey("damage_reduction") || mobTempStats.containsKey("damage_reduction")) {

            // Set base stat
            Integer damageReductionValue = (Integer) mobStats.getOrDefault("damage_reduction",0);

            // Calculate final reduction value with temp stat
            damageReductionValue += mobTempStats.getOrDefault("damage_reduction",0);

            modifyDamage(damage, null, damageReductionValue, internalName);
        }
    }

    private <T> void modifyDamage(DamageMetadata damage, T type, Integer statValue, String internalName){
        // For logging because Element returns io.lumine.mythic.lib.element.Element@12345 without .getName() >:(
        String typeName = (type instanceof Element) ? ((Element) type).getName() : Objects.requireNonNullElse(type, "GENERAL").toString();
        double originalDamage = damage.getDamage();

        // Do reduction when its bigger than 0
        if(statValue > 0) {
            // Convert to float for calculating modifier
            float damageReduction = statValue / 100f;
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
            debugLogger("Applied " + typeName + " reduction: " + statValue + "%");
            debugLogger("Damage changes: " + originalDamage + " -> " + damage.getDamage());
        }
        // Do amplification when its negative
        else if(statValue < 0){
            statValue = Math.abs(statValue);
            // Convert to float for calculating modifier
            float damageAmplification = statValue / 100f;
            float finalAmplification = Math.max(1 + damageAmplification, 0);

            switch (type) {
                // Apply the general damage reduction modifier
                case null -> damage.multiplicativeModifier(finalAmplification);
                // Apply the other damage reduction modifier
                case DamageType damageType -> damage.multiplicativeModifier(finalAmplification, damageType);
                // Apply the elemental damage reduction modifier
                case Element element -> damage.multiplicativeModifier(finalAmplification, element);
                default -> logger.warning("Failed to perform damage amplification: Unknown damage type!");
            }

            // Log reduction for debugging
            debugLogger("Applied " + typeName + " amplification: " + statValue + "%");
            debugLogger("Damage changes: " + originalDamage + " -> " + damage.getDamage());
        }
    }

    public void debugLogger(String debugMessage){
        if(plugin.getConfig().getBoolean("debug",false))
            logger.info(debugMessage);
    }

}
