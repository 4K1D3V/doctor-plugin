package pro.akii.ks.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pro.akii.ks.DoctorPlugin;
import pro.akii.ks.utils.ConfigUtil;
import pro.akii.ks.utils.NMSUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player injuries with a centralized ticking system.
 */
public class InjuryManager {

    private final DoctorPlugin plugin;
    private final Map<UUID, Long> brokenLegs;
    private final Map<UUID, Long> infections;
    private final Map<UUID, Long> bleedings;
    private final BukkitRunnable tickTask;

    public InjuryManager(DoctorPlugin plugin) {
        this.plugin = plugin;
        this.brokenLegs = new HashMap<>();
        this.infections = new HashMap<>();
        this.bleedings = new HashMap<>();
        this.tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    if (infections.containsKey(uuid)) {
                        if (currentTime - infections.get(uuid) > ConfigUtil.getInfectionDuration() * 1000) {
                            infections.remove(uuid);
                        } else if ((currentTime - infections.get(uuid)) % (ConfigUtil.getInfectionDamageInterval() * 50) == 0) {
                            player.damage(0.5);
                        }
                    }

                    if (bleedings.containsKey(uuid)) {
                        if (currentTime - bleedings.get(uuid) > ConfigUtil.getBleedingDuration() * 1000) {
                            bleedings.remove(uuid);
                        } else {
                            NMSUtil.applyBleedingEffect(player);
                            player.damage(ConfigUtil.getBleedingDamage());
                        }
                    }

                    if (brokenLegs.containsKey(uuid) && currentTime - brokenLegs.get(uuid) > ConfigUtil.getBrokenLegDuration() * 1000) {
                        NMSUtil.cureBrokenLeg(player);
                        brokenLegs.remove(uuid);
                    }
                }
            }
        };
        tickTask.runTaskTimer(plugin, 0L, ConfigUtil.getInjuryTickRate() * 20L);
    }

    public void applyBrokenLeg(Player player) {
        NMSUtil.applyBrokenLeg(player);
        brokenLegs.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void applyInfection(Player player) {
        infections.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void applyBleeding(Player player) {
        bleedings.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void cureInjury(Player player, String injuryType) {
        UUID uuid = player.getUniqueId();
        switch (injuryType.toLowerCase()) {
            case "brokenleg":
                NMSUtil.cureBrokenLeg(player);
                brokenLegs.remove(uuid);
                break;
            case "infection":
                infections.remove(uuid);
                break;
            case "bleeding":
                bleedings.remove(uuid);
                break;
            default:
                plugin.getLogger().warning("Unknown injury type: " + injuryType);
        }
    }

    public boolean hasInjury(Player player, String injuryType) {
        UUID uuid = player.getUniqueId();
        return switch (injuryType.toLowerCase()) {
            case "brokenleg" -> brokenLegs.containsKey(uuid);
            case "infection" -> infections.containsKey(uuid);
            case "bleeding" -> bleedings.containsKey(uuid);
            default -> false;
        };
    }

    public void shutdown() {
        tickTask.cancel();
    }
}