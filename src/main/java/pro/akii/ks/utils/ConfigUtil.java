package pro.akii.ks.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

/**
 * Utility class for accessing configuration values and items.
 */
public class ConfigUtil {

    private static FileConfiguration config;

    public static void initialize(FileConfiguration config) {
        ConfigUtil.config = config;
    }

    // Doctor Settings
    public static int getXpPerHeal() { return config.getInt("doctor.xp-per-heal", 10); }
    public static int getMaxLevel() { return config.getInt("doctor.max-level", 5); }
    public static int getHealCooldown() { return config.getInt("doctor.heal-cooldown-seconds", 30); }
    public static double getTeamHealCooldownBuff() { return config.getDouble("doctor.team-heal-cooldown-buff", 0.8); }
    public static double getTeamXpPoolShare() { return config.getDouble("doctor.team-xp-pool-share", 0.5); }
    public static double getTeamXpBuffMultiplier() { return config.getDouble("doctor.team-xp-buff-multiplier", 0.0002); }
    public static int getDiagnoseCooldown() { return config.getInt("doctor.diagnose-cooldown-seconds", 10); }

    // Injury Settings
    public static double getInjuryChance() { return config.getDouble("injury.chance-per-damage", 0.1); }
    public static int getInjuryTickRate() { return config.getInt("injury.tick-rate-seconds", 1); }
    public static int getBrokenLegDuration() { return config.getInt("injury.broken-leg-duration-seconds", 60); }
    public static int getInfectionDuration() { return config.getInt("injury.infection-duration-seconds", 120); }
    public static int getInfectionDamageInterval() { return config.getInt("injury.infection-damage-interval", 20); }
    public static int getBleedingDuration() { return config.getInt("injury.bleeding-duration-seconds", 90); }
    public static double getBleedingDamage() { return config.getDouble("injury.bleeding-damage", 1.0); }

    // Emergency Settings
    public static int getEmergencyInterval() { return config.getInt("emergency.interval-minutes", 30); }
    public static double getPlagueChance() { return config.getDouble("emergency.plague-chance", 0.33); }
    public static double getBleedingChance() { return config.getDouble("emergency.bleeding-chance", 0.33); }
    public static double getFractureChance() { return config.getDouble("emergency.fracture-chance", 0.34); }

    public static FileConfiguration getConfig() { return config; }

    // Item Definitions
    public static ItemStack getStethoscopeItem() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("items.stethoscope.material", "IRON_INGOT")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(config.getString("items.stethoscope.display-name", "§fStethoscope"));
        meta.setLore(Collections.singletonList(config.getString("items.stethoscope.lore", "§7Diagnose players' health")));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getBandageItem() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("items.bandage.material", "PAPER")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(config.getString("items.bandage.display-name", "§fBandage"));
        meta.setLore(Collections.singletonList(config.getString("items.bandage.lore", "§7Heals HP (no injuries)")));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getAntidoteItem() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("items.antidote.material", "POTION")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(config.getString("items.antidote.display-name", "§fAntidote"));
        meta.setLore(Collections.singletonList(config.getString("items.antidote.lore", "§7Cures Infection")));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getSutureKitItem() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("items.suture-kit.material", "STRING")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(config.getString("items.suture-kit.display-name", "§fSuture Kit"));
        meta.setLore(Collections.singletonList(config.getString("items.suture-kit.lore", "§7Stops Bleeding")));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getSplintItem() {
        ItemStack item = new ItemStack(Material.valueOf(config.getString("items.splint.material", "STICK")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(config.getString("items.splint.display-name", "§fSplint"));
        meta.setLore(Collections.singletonList(config.getString("items.splint.lore", "§7Fixes Broken Leg")));
        item.setItemMeta(meta);
        return item;
    }
}