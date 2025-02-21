package pro.akii.ks.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pro.akii.ks.DoctorPlugin;
import pro.akii.ks.utils.ConfigUtil;

/**
 * Schedules and manages server-wide emergency events.
 */
public class EmergencyManager {

    private final DoctorPlugin plugin;
    private final BukkitRunnable emergencyTask;

    public EmergencyManager(DoctorPlugin plugin) {
        this.plugin = plugin;
        this.emergencyTask = new BukkitRunnable() {
            @Override
            public void run() {
                double roll = Math.random();
                if (roll < ConfigUtil.getPlagueChance()) {
                    triggerPlagueEvent();
                } else if (roll < ConfigUtil.getPlagueChance() + ConfigUtil.getBleedingChance()) {
                    triggerBleedingEvent();
                } else if (roll <= ConfigUtil.getPlagueChance() + ConfigUtil.getBleedingChance() + ConfigUtil.getFractureChance()) {
                    triggerFractureEvent();
                }
            }
        };
        emergencyTask.runTaskTimer(plugin, 0L, ConfigUtil.getEmergencyInterval() * 20L * 60L);
    }

    private void triggerPlagueEvent() {
        Bukkit.broadcastMessage("§cA Plague outbreak has begun! Doctors are needed!");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getDoctorManager().isDoctor(player) && Math.random() < 0.5) {
                plugin.getInjuryManager().applyInfection(player);
            }
        }
        new BukkitRunnable() {
            int duration = 300; // 5 minutes
            @Override
            public void run() {
                if (duration-- <= 0) {
                    Bukkit.broadcastMessage("§aThe Plague outbreak has subsided.");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void triggerBleedingEvent() {
        Bukkit.broadcastMessage("§cA Mass Bleeding event has started! Doctors, act quickly!");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getDoctorManager().isDoctor(player) && Math.random() < 0.5) {
                plugin.getInjuryManager().applyBleeding(player);
            }
        }
        new BukkitRunnable() {
            int duration = 300; // 5 minutes
            @Override
            public void run() {
                if (duration-- <= 0) {
                    Bukkit.broadcastMessage("§aThe Mass Bleeding event has ended.");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void triggerFractureEvent() {
        Bukkit.broadcastMessage("§cA Mass Fracture event has struck! Doctors, mobilize!");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getDoctorManager().isDoctor(player) && Math.random() < 0.5) {
                plugin.getInjuryManager().applyBrokenLeg(player);
            }
        }
        new BukkitRunnable() {
            int duration = 300; // 5 minutes
            @Override
            public void run() {
                if (duration-- <= 0) {
                    Bukkit.broadcastMessage("§aThe Mass Fracture event has concluded.");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void shutdown() {
        emergencyTask.cancel();
    }
}