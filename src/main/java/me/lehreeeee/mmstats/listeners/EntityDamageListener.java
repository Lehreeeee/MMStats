package me.lehreeeee.mmstats.listeners;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import me.lehreeeee.mmstats.MMStats;
import me.lehreeeee.mmstats.managers.MobStatsManager;
import me.lehreeeee.mmstats.managers.MythicMobsManager;
import me.lehreeeee.mmstats.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMobAttack(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // Handle projectile
        if (damager instanceof Projectile proj) {
            // Set damager to arrow shooter instead of arrow
            if(proj.getShooter() instanceof Entity) damager = (Entity) proj.getShooter();
        }

        // Ignore dead entity or player attack
        if(!(damager instanceof LivingEntity) || damager instanceof Player) return;
        UUID damagerUUID = damager.getUniqueId();

        // Imagine having no stat
        if(!mobStatsManager.hasMobTempStats(damagerUUID)){
            LoggerUtil.debug("Damager " + damager.getName() + " does not have temp stats.");
            return;
        }

        Double weakenedValue = mobStatsManager.getMobTempStats(damagerUUID).get("weakened");
        if(weakenedValue == null){
            LoggerUtil.debug("Damager " + damager.getName() + " does not have \"weakened\" stat, skipping.");
        } else{
            double damageReduction = (1 - weakenedValue / 100f);
            DamageMetadata damageMetadata = MythicLib.inst().getDamage().findAttack(event).getDamage();
            damageMetadata.multiplicativeModifier(Math.max(damageReduction, 0));

            LoggerUtil.debug("Applied damage reduction to weakened mob's attack: " + weakenedValue + "%");
            LoggerUtil.debug("Damage changes: " + event.getDamage() + " -> " + damageMetadata.getDamage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAttack(PlayerAttackEvent event) {
        AttackMetadata attack = event.getAttack();
        LivingEntity victim = attack.getTarget();
        UUID uuid = victim.getUniqueId();

        // Is it a mythicmobs?
        if(!mythicMobsManager.isMythicMob(victim)) {
            LoggerUtil.debug("Victim " + victim.getName() + " is not mythicmobs.");
            return;
        }

        // Get internal name of this mob
        String internalName = mythicMobsManager.getInternalName(uuid);

        // Imagine having no stat
        if(!mobStatsManager.hasMobStats(internalName) && !mobStatsManager.hasMobTempStats(uuid)){
            LoggerUtil.debug("Victim " + victim.getName() + " does not have stats.");
            return;
        }

        // Get damage meta data
        DamageMetadata damage = attack.getDamage();

        // Get the stats of the mob
        Map<String, Object> mobStats = mobStatsManager.getMobStats(internalName);
        Map<String, Double> mobTempStats = mobStatsManager.getMobTempStats(uuid);

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
        LoggerUtil.debug( "Damage Types: " + damageTypes + " Element Types: " + elementStrings);

        // Iterate through all damage types and perform reduction
        for(DamageType damageType : damageTypes) {
            String key = damageType.toString().toLowerCase() + "_reduction";
            Double damageReductionValue = 0D;

            // Set base stat if exists
            if(mobStats.containsKey(key)) damageReductionValue = (Double) mobStats.get(key);

            // Calculate final reduction value with temp stat
            if(mobTempStats.containsKey(key)) damageReductionValue += mobTempStats.get(key);

            modifyDamage(damage, damageType, damageReductionValue, internalName);

        }

        // Iterate through all elemental damage types and perform reduction
        if(!elementTypes.isEmpty() && (mobStats.containsKey("elements") || mobStatsManager.hasMobTempElementalStats(uuid))){
            Object elementsObj = mobStats.get("elements");
            Map<String, Double> elementStats = new HashMap<>();

            // Cast Obj back to Map
            if(elementsObj instanceof Map){
                try{
                    elementStats = (Map<String, Double>) elementsObj;
                }
                catch(ClassCastException ex){
                    logger.warning(ex.getMessage());
                }
            }

            // Iterate through all damage types and perform damage reduction
            for (Element elementType : elementTypes) {
                String key = elementType.getName().toLowerCase() + "_reduction";
                Double damageReductionValue = 0D;

                // Set base stat if exists
                if (elementStats.containsKey(key)) damageReductionValue = elementStats.getOrDefault(key,0D);

                // Calculate final reduction value with temp stat
                if(mobTempStats.containsKey("elements." + key)) damageReductionValue += mobTempStats.get("elements." + key);

                modifyDamage(damage, elementType, damageReductionValue, internalName);
            }
        }

        // General damage
        if(mobStats.containsKey("damage_reduction") || mobTempStats.containsKey("damage_reduction")) {

            // Set base stat
            Double damageReductionValue = (Double) mobStats.getOrDefault("damage_reduction",0D);

            // Calculate final reduction value with temp stat
            damageReductionValue += mobTempStats.getOrDefault("damage_reduction",0D);

            modifyDamage(damage, null, damageReductionValue, internalName);
        }
    }

    private <T> void modifyDamage(DamageMetadata damage, T type, double statValue, String internalName){
        // For logging because Element returns io.lumine.mythic.lib.element.Element@12345 without .getName() >:(
        String typeName = (type instanceof Element) ? ((Element) type).getName() : Objects.requireNonNullElse(type, "GENERAL").toString();
        double originalDamage = damage.getDamage();

        // Do reduction when its bigger than 0
        if(statValue > 0) {
            // Convert to float for calculating modifier
            double damageReduction = statValue / 100D;
            double finalReduction = Math.max(1 - damageReduction, 0);

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
            LoggerUtil.debug("Applied " + typeName + " reduction: " + statValue + "%");
            LoggerUtil.debug("Damage changes: " + originalDamage + " -> " + damage.getDamage());
        }
        // Do amplification when its negative
        else if(statValue < 0){
            statValue = Math.abs(statValue);
            // Convert to float for calculating modifier
            double damageAmplification = statValue / 100F;
            double finalAmplification = Math.max(1 + damageAmplification, 0);

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
            LoggerUtil.debug("Applied " + typeName + " amplification: " + statValue + "%");
            LoggerUtil.debug("Damage changes: " + originalDamage + " -> " + damage.getDamage());
        }
    }
}
